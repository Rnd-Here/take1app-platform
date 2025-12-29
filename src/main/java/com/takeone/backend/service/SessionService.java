package com.takeone.backend.service;

import com.takeone.backend.entity.Session;
import com.takeone.backend.entity.User;
import com.takeone.backend.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final int SESSION_EXPIRY_DAYS = 30;
    private static final int TOKEN_LENGTH = 64; // bytes, results in 88 char base64 string
    private static final SecureRandom secureRandom = new SecureRandom();
    private final SessionRepository sessionRepository;
    private final DeviceTokenService deviceTokenService;

    /**
     * Create a new session for a user
     */
    @Transactional
    public Session createSession(User user, HttpServletRequest request) {
        String refreshToken = generateSecureToken();
        String deviceId = extractDeviceId(request);
        String ipAddress = extractIpAddress(request);

        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setDeviceId(deviceId);
        session.setIpAddress(ipAddress);
        session.setExpiresAt(LocalDateTime.now().plusDays(SESSION_EXPIRY_DAYS));
        session.setIsActive(true);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastAccessedAt(LocalDateTime.now());

        session = sessionRepository.save(session);
        log.info("Created new session for user: {}, deviceId: {}", user.getUsername(), deviceId);

        return session;
    }

    /**
     * Validate session token and refresh last accessed time
     */
    @Transactional
    public Session validateAndRefreshSession(String token, HttpServletRequest request) {
        Optional<Session> sessionOpt = sessionRepository.findByRefreshTokenAndIsActiveTrue(token);

        if (sessionOpt.isEmpty()) {
            log.debug("Session not found or inactive for token");
            return null;
        }

        Session session = sessionOpt.get();

        // Ensure user is loaded (JOIN FETCH in query should handle this, but being
        // explicit)
        User user = session.getUser();
        if (user == null) {
            log.warn("Session has null user reference");
            return null;
        }

        // Explicitly initialize the user proxy to avoid lazy loading issues
        // This is safer than calling getUsername() and ignoring the result
        Hibernate.initialize(user);

        // Check if session has expired
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Session expired for user: {}", session.getUser().getUsername());
            session.setIsActive(false);
            sessionRepository.save(session);
            return null;
        }

        // Update last accessed time and IP if changed
        session.setLastAccessedAt(LocalDateTime.now());
        String currentIp = extractIpAddress(request);
        if (!currentIp.equals(session.getIpAddress())) {
            session.setIpAddress(currentIp);
        }

        sessionRepository.save(session);

        return session;
    }

    /**
     * Refresh session token and extend expiry
     */
    @Transactional
    public Session refreshSession(String oldToken, HttpServletRequest request) {
        Optional<Session> sessionOpt = sessionRepository.findByRefreshTokenAndIsActiveTrue(oldToken);

        if (sessionOpt.isEmpty()) {
            log.warn("Attempting to refresh non-existent or inactive session");
            return null;
        }

        Session session = sessionOpt.get();

        // Check if session has expired
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Cannot refresh expired session for user: {}", session.getUser().getUsername());
            session.setIsActive(false);
            sessionRepository.save(session);
            return null;
        }

        // Generate new token and extend expiry
        String newToken = generateSecureToken();
        session.setRefreshToken(newToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(SESSION_EXPIRY_DAYS));
        session.setLastAccessedAt(LocalDateTime.now());

        String currentIp = extractIpAddress(request);
        if (!currentIp.equals(session.getIpAddress())) {
            session.setIpAddress(currentIp);
        }

        session = sessionRepository.save(session);
        log.info("Refreshed session for user: {}", session.getUser().getUsername());

        return session;
    }

    /**
     * Logout - invalidate session
     */
    @Transactional
    public void invalidateSession(String token) {
        Optional<Session> sessionOpt = sessionRepository.findByRefreshTokenAndIsActiveTrue(token);

        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            session.setIsActive(false);
            sessionRepository.save(session);

            // Also deactivate the FCM token for this specific device
            if (session.getUser() != null && session.getDeviceId() != null) {
                deviceTokenService.deactivateToken(session.getUser().getId(), session.getDeviceId());
            }

            if (session.getUser() != null) {
                log.info("Invalidated session and deactivated FCM token for user: {}", session.getUser().getUsername());
            }
        }
    }

    /**
     * Logout all sessions for a user
     */
    @Transactional
    public void invalidateAllUserSessions(Long userId) {
        int count = sessionRepository.deactivateAllUserSessions(userId);
        deviceTokenService.deactivateAllTokens(userId);
        log.info("Invalidated {} sessions and all FCM tokens for userId: {}", count, userId);
    }

    /**
     * Validate session token without database update (for validate-session
     * endpoint)
     */
    @Transactional(readOnly = true)
    public Session validateSessionToken(String token) {
        Optional<Session> sessionOpt = sessionRepository.findByRefreshTokenAndIsActiveTrue(token);

        if (sessionOpt.isEmpty()) {
            return null;
        }

        Session session = sessionOpt.get();

        // Check if session has expired
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        return session;
    }

    /**
     * Clean up expired sessions (can be scheduled)
     */
    @Transactional
    public void cleanupExpiredSessions() {
        // 1. Deactivate currently expired sessions
        int deactivatedCount = sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
        log.info("Deactivated {} newly expired sessions", deactivatedCount);

        // 2. Hard delete sessions that have been inactive/deactivated for more than 60
        // days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(60);
        int deletedCount = sessionRepository.deleteOldInactiveSessions(cutoffDate);
        log.info("Hard deleted {} sessions inactive since {}", deletedCount, cutoffDate);
    }

    /**
     * Generate cryptographically secure random token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Extract device identifier from request
     */
    private String extractDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String deviceId = request.getHeader("X-Device-Id");

        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }

        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * Extract IP address from request, considering proxy headers
     */
    private String extractIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
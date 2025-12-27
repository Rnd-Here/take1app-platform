package com.takeone.backend.service;

import com.takeone.backend.model.Session;
import com.takeone.backend.model.User;
import com.takeone.backend.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Session createSession(User user, String ipAddress, String deviceId) {
        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(UUID.randomUUID().toString());
        session.setIpAddress(ipAddress);
        session.setDeviceId(deviceId);
        session.setExpiresAt(LocalDateTime.now().plusDays(30)); // 30 days expiration
        return sessionRepository.save(session);
    }

    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void revokeSession(String refreshToken) {
        sessionRepository.deleteByRefreshToken(refreshToken);
    }

    @Transactional
    public void revokeSessionById(Long sessionId, Long userId) {
        Optional<Session> session = sessionRepository.findById(sessionId);
        if (session.isPresent() && session.get().getUser().getId().equals(userId)) {
            sessionRepository.delete(session.get());
        }
    }

    @Transactional
    public void revokeAllUserSessions(User user) {
        List<Session> sessions = sessionRepository.findByUserAndIsActiveTrue(user);
        sessionRepository.deleteAll(sessions);
    }

    public List<Session> getActiveSessions(User user) {
        return sessionRepository.findByUserAndIsActiveTrue(user);
    }

    public boolean isSessionValid(Session session) {
        return session.isActive() && session.getExpiresAt().isAfter(LocalDateTime.now());
    }
}

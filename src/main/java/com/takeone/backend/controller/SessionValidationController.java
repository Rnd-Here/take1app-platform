package com.takeone.backend.controller;

import com.takeone.backend.dto.SessionValidationResponse;
import com.takeone.backend.entity.Session;
import com.takeone.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionValidationController {

    private final SessionService sessionService;

    /**
     * Validate session endpoint - can be used by other services or for health checks
     * This endpoint is public (no authentication required)
     */
    @PostMapping("/validate-session")
    public ResponseEntity<SessionValidationResponse> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken
    ) {
        try {
            String token = extractToken(authHeader, sessionToken);

            if (!StringUtils.hasText(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SessionValidationResponse.builder()
                                .valid(false)
                                .message("No session token provided")
                                .build());
            }

            Session session = sessionService.validateSessionToken(token);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(SessionValidationResponse.builder()
                                .valid(false)
                                .message("Invalid or expired session")
                                .build());
            }

            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(true)
                    .userId(session.getUser().getId())
                    .username(session.getUser().getUsername())
                    .email(session.getUser().getEmail())
                    .accountType(session.getUser().getAccountType())
                    .expiresAt(session.getExpiresAt())
                    .message("Session is valid")
                    .build());

        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SessionValidationResponse.builder()
                            .valid(false)
                            .message("Error validating session")
                            .build());
        }
    }

    /**
     * Get session info for current authenticated user
     */
    @GetMapping("/session-info")
    public ResponseEntity<SessionValidationResponse> getSessionInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken
    ) {
        try {
            String token = extractToken(authHeader, sessionToken);

            if (!StringUtils.hasText(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SessionValidationResponse.builder()
                                .valid(false)
                                .message("No session token provided")
                                .build());
            }

            Session session = sessionService.validateSessionToken(token);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(SessionValidationResponse.builder()
                                .valid(false)
                                .message("Invalid or expired session")
                                .build());
            }

            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(true)
                    .userId(session.getUser().getId())
                    .username(session.getUser().getUsername())
                    .email(session.getUser().getEmail())
                    .accountType(session.getUser().getAccountType())
                    .deviceId(session.getDeviceId())
                    .ipAddress(session.getIpAddress())
                    .createdAt(session.getCreatedAt())
                    .lastAccessedAt(session.getLastAccessedAt())
                    .expiresAt(session.getExpiresAt())
                    .message("Session is valid")
                    .build());

        } catch (Exception e) {
            log.error("Error getting session info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SessionValidationResponse.builder()
                            .valid(false)
                            .message("Error retrieving session info")
                            .build());
        }
    }

    private String extractToken(String authHeader, String sessionToken) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return sessionToken;
    }
}
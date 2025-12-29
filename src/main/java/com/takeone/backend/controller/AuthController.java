package com.takeone.backend.controller;

import com.takeone.backend.dto.AuthRequest;
import com.takeone.backend.dto.AuthResponse;
import com.takeone.backend.dto.RefreshTokenRequest;
import com.takeone.backend.entity.Session;
import com.takeone.backend.entity.User;
import com.takeone.backend.service.AuthService;
import com.takeone.backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    /**
     * Authenticate user with Firebase token
     * 1. Validate Firebase token
     * 2. Create or update user in database
     * 3. Revoke all previous sessions
     * 4. Create new session
     * 5. Return session token and user details
     */
    @PostMapping("/token")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            log.info("Authentication request received");

            // Authenticate and get/create user
            User user = authService.authenticateWithFirebase(request);

            // Revoke all previous sessions for this user
            sessionService.invalidateAllUserSessions(user.getId());
            log.info("Revoked all previous sessions for user: {}", user.getUsername());

            // Create new session
            Session session = sessionService.createSession(user, httpRequest);

            // Build response
            AuthResponse response = AuthResponse.builder()
                    .sessionToken(session.getRefreshToken())
                    .expiresAt(session.getExpiresAt())
                    .user(authService.buildUserResponse(user))
                    .build();

            log.info("User authenticated successfully: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .error("Authentication failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Refresh session token
     * Extends session expiry and returns new token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            log.info("Token refresh request received");

            Session session = sessionService.refreshSession(request.getRefreshToken(), httpRequest);

            if (session == null) {
                log.warn("Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.builder()
                                .error("Invalid or expired session")
                                .build());
            }

            AuthResponse response = AuthResponse.builder()
                    .sessionToken(session.getRefreshToken())
                    .expiresAt(session.getExpiresAt())
                    .user(authService.buildUserResponse(session.getUser()))
                    .build();

            log.info("Token refreshed successfully for user: {}", session.getUser().getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .error("Token refresh failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Logout current session
     * Invalidates the session token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);

            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid authorization header"));
            }

            sessionService.invalidateSession(token);
            log.info("User logged out successfully");

            return ResponseEntity.ok(new MessageResponse("Logged out successfully"));

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Logout failed: " + e.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    // Simple response classes
    record MessageResponse(String message) {
    }

    record ErrorResponse(String error) {
    }
}
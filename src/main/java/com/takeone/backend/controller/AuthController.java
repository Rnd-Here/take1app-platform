package com.takeone.backend.controller;

import com.takeone.backend.dto.AuthResponse;
import com.takeone.backend.dto.TokenRequest;
import com.takeone.backend.model.Session;
import com.takeone.backend.model.User;
import com.takeone.backend.repository.UserRepository;
import com.takeone.backend.service.FirebaseService;
import com.takeone.backend.service.SessionService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final FirebaseService firebaseService;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public ResponseEntity<?> authenticate(@RequestBody @Valid TokenRequest request, HttpServletRequest httpRequest) {
        try {
            FirebaseToken decodedToken = firebaseService.verifyToken(request.getToken());
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String mobile = (String) decodedToken.getClaims().get("phone_number");
            String name = decodedToken.getName();

            Optional<User> userOpt = userRepository.findByUid(uid);
            User user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
                // Sync details. Trust Firebase token as source of truth.
                // If the token has new info (e.g. from linkWithCredential), update our DB.
                if (email != null && !email.equals(user.getEmail()))
                    user.setEmail(email);
                if (mobile != null && !mobile.equals(user.getMobile()))
                    user.setMobile(mobile);

                // Update verification status from claims
                Boolean emailVerified = (Boolean) decodedToken.getClaims().get("email_verified");
                if (emailVerified != null)
                    user.setIsEmailVerified(emailVerified);

                Boolean phoneVerified = (Boolean) decodedToken.getClaims().get("phone_number_verified");
                if (phoneVerified != null) {
                    user.setIsPhoneVerified(phoneVerified);
                } else if (mobile != null) {
                    // Fallback: assume verified if mobile number is present in Firebase token
                    user.setIsPhoneVerified(true);
                }

                // Update profile picture if present in token and not in DB (or overwrite?)
                String picture = (String) decodedToken.getClaims().get("picture");
                if (picture != null)
                    user.setProfilePictureUrl(picture);

                user.setLastLogin(java.time.LocalDateTime.now());
                user = userRepository.save(user);
            } else {
                user = new User();
                user.setUid(uid);
                user.setEmail(email);
                user.setMobile(mobile);
                // Split name into first/last if possible, or just set display name
                if (name != null) {
                    user.setDisplayName(name); // Use display name for the full name from provider
                    // Simple split attempt for first/last
                    String[] parts = name.split(" ", 2);
                    if (parts.length > 0)
                        user.setFirstName(parts[0]);
                    if (parts.length > 1)
                        user.setLastName(parts[1]);
                }

                Boolean emailVerified = (Boolean) decodedToken.getClaims().get("email_verified");
                if (emailVerified != null)
                    user.setIsEmailVerified(emailVerified);
                if (mobile != null)
                    user.setIsPhoneVerified(true);

                String picture = (String) decodedToken.getClaims().get("picture");
                if (picture != null)
                    user.setProfilePictureUrl(picture);

                user.setLastLogin(java.time.LocalDateTime.now());
                user = userRepository.save(user);
            }

            Session session = sessionService.createSession(user, httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent"));

            AuthResponse response = new AuthResponse();
            response.setSessionToken(session.getRefreshToken());
            response.setUser(user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid Token");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        // Implementation for simple refresh if we were using short-lived access tokens.
        // Since we use the session token (refreshToken) as the main auth token,
        // this might just validate it or issue a new one (rotation).
        // For now, simple validation.
        if (token != null && token.startsWith("Bearer ")) {
            String sessionToken = token.substring(7);
            Optional<Session> session = sessionService.findByRefreshToken(sessionToken);
            if (session.isPresent() && sessionService.isSessionValid(session.get())) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.status(401).body("Invalid Token");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String sessionToken = token.substring(7);
            sessionService.revokeSession(sessionToken);
        }
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@AuthenticationPrincipal User user) {
        if (user != null) {
            sessionService.revokeAllUserSessions(user);
            return ResponseEntity.ok("Logged out all devices");
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(@AuthenticationPrincipal User user) {
        List<Session> sessions = sessionService.getActiveSessions(user);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> revokeSession(@PathVariable Long id, @AuthenticationPrincipal User user) {
        sessionService.revokeSessionById(id, user.getId());
        return ResponseEntity.ok("Session revoked");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@AuthenticationPrincipal User user) {
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).build();
    }
}

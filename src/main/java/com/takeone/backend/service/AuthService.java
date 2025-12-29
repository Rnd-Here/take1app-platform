package com.takeone.backend.service;

import com.google.firebase.auth.FirebaseToken;
import com.takeone.backend.dto.AuthRequest;
import com.takeone.backend.dto.UserResponse;
import com.takeone.backend.entity.AccountType;
import com.takeone.backend.entity.User;
import com.takeone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    /**
     * Authenticate user with Firebase token
     * Creates or updates user in database
     */
    @Transactional
    public User authenticateWithFirebase(AuthRequest request) throws Exception {
        // Verify Firebase token
        FirebaseToken decodedToken = firebaseService.verifyToken(request.getIdToken());
        String uid = decodedToken.getUid();

        log.info("Firebase token verified for UID: {}", uid);

        // Check if user exists
        Optional<User> existingUser = userRepository.findByUid(uid);

        // Update existing user
        // Create new user
        return existingUser.map(user -> updateExistingUser(user, decodedToken, request))
                .orElseGet(() -> createNewUser(decodedToken, request));
    }

    /**
     * Update existing user with Firebase details
     * UI might have done linkWithCredentials and updated details
     */
    private User updateExistingUser(User user, FirebaseToken token, AuthRequest request) {
        log.info("Updating existing user: {}", user.getUid());

        // Update email and verification status
        String email = token.getEmail();
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
            user.setIsEmailVerified(token.isEmailVerified());
        }

        // Update phone and verification status
        Object phoneClaim = token.getClaims().get("phone_number");
        if (phoneClaim != null) {
            String phone = phoneClaim.toString();
            if (!phone.isEmpty()) {
                user.setMobile(phone);
                user.setIsPhoneVerified(true);
            }
        }

        // Update name if available from token
        String name = token.getName();
        if (name != null && !name.isEmpty() && user.getDisplayName() == null) {
            user.setDisplayName(name);
        }

        // Update profile picture if available
        String picture = token.getPicture();
        if (picture != null && !picture.isEmpty() && user.getProfilePictureUrl() == null) {
            user.setProfilePictureUrl(picture);
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getUid());

        return savedUser;
    }

    /**
     * Create new user from Firebase token
     * Saves incomplete user information
     */
    private User createNewUser(FirebaseToken token, AuthRequest request) {
        log.info("Creating new user for UID: {}", token.getUid());

        User user = new User();

        // Essential fields from Firebase
        user.setUid(token.getUid());
        user.setEmail(token.getEmail());
        user.setIsEmailVerified(token.isEmailVerified());

        // Phone number from Firebase
        Object phoneClaim = token.getClaims().get("phone_number");
        if (phoneClaim != null) {
            String phone = phoneClaim.toString();
            if (!phone.isEmpty()) {
                user.setMobile(phone);
                user.setIsPhoneVerified(true);
            } else {
                user.setMobile(null);
                user.setIsPhoneVerified(false);
            }
        } else {
            user.setMobile(null);
            user.setIsPhoneVerified(false);
        }

        // Display name from Firebase
        String name = token.getName();
        if (name != null && !name.isEmpty()) {
            user.setDisplayName(name);
        }
        // Profile picture from Firebase
        String picture = token.getPicture();
        if (picture != null && !picture.isEmpty()) {
            user.setProfilePictureUrl(picture);
        }
        // Generate temporary username from UID (user can change later)
        user.setUsername(generateTemporaryUsername(token.getUid()));

        // Set default values
        user.setAccountType(AccountType.NEW_USER); // Default, user can change in profile setup
        user.setIsPortfolioCreated(false);
        user.setIsActive(true);
        user.setLastLogin(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("New user created successfully: {}", savedUser.getUid());
        return savedUser;
    }

    /**
     * Generate temporary username from Firebase UID
     */
    private String generateTemporaryUsername(String uid) {
        // Take first 8 characters of UID and prefix with 'user_'
        String shortUid = uid.length() > 8 ? uid.substring(0, 8) : uid;
        return "user_" + shortUid;
    }

    /**
     * Build user response DTO for API
     * Only includes non-sensitive information needed by UI
     */
    public UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .uid(user.getUid())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .accountType(user.getAccountType())
                .isPortfolioCreated(user.getIsPortfolioCreated())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .isActive(user.getIsActive())
                .build();
    }
}
package com.takeone.backend.service;

import com.takeone.backend.dto.UserProfileRequest;
import com.takeone.backend.dto.UserProfileResponse;
import com.takeone.backend.entity.User;
import com.takeone.backend.repository.UserRepository;
import com.takeone.backend.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UsernameService usernameService;

    private static @NonNull String getNormalizedUsername(String username) {
        String normalizedUsername = username.toLowerCase().trim();

        // Check length
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        // Check format (alphanumeric and underscore only)
        if (!normalizedUsername.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain lowercase letters, numbers, and underscores");
        }
        return normalizedUsername;
    }

    /**
     * Get user profile with Redis caching
     * Cache key: "profiles::userId"
     */
    @Cacheable(value = "profiles", key = "#userId")
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Fetching profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildProfileResponse(user);
    }

    /**
     * Create user profile (first time setup)
     * Validates username uniqueness and sets profile data
     */
    @CacheEvict(value = "profiles", key = "#userId")
    @Transactional
    public UserProfileResponse createUserProfile(Long userId, UserProfileRequest request) {
        log.info("Creating profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if profile already created
        if (user.getIsPortfolioCreated()) {
            throw new IllegalArgumentException("Profile already created. Use PUT to update.");
        }

        // Validate and set username
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            validateAndSetUsername(user, request.getUsername());
        }

        // Set profile fields
        updateUserFields(user, request);

        // portfolio field remains false unless User starts creating portfolio
        user.setIsPortfolioCreated(false);

        User savedUser = userRepository.save(user);

        // Invalidate username cache
        if (request.getUsername() != null) {
            usernameService.invalidateUsernameCache(request.getUsername());
        }

        log.info("Profile created successfully for userId: {}", userId);
        return buildProfileResponse(savedUser);
    }

    /**
     * Update user profile
     * Allows updating existing profile information
     */
    @CacheEvict(value = "profiles", key = "#userId")
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        log.info("Updating profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If username is being changed, validate it
        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUsername())) {
            String oldUsername = user.getUsername();
            validateAndSetUsername(user, request.getUsername());

            // Invalidate both old and new username caches
            if (oldUsername != null) {
                usernameService.invalidateUsernameCache(oldUsername);
            }
            usernameService.invalidateUsernameCache(request.getUsername());
        }

        // Update profile fields
        updateUserFields(user, request);

        User savedUser = userRepository.save(user);

        log.info("Profile updated successfully for userId: {}", userId);
        return buildProfileResponse(savedUser);
    }

    /**
     * Validate username and set it with hash
     */
    private void validateAndSetUsername(User user, String username) {
        // Normalize username
        String normalizedUsername = getNormalizedUsername(username);

        // Check availability
        if (!usernameService.isUsernameAvailable(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Set username and hash
        user.setUsername(normalizedUsername);
        user.setUsernameHash(HashUtil.sha256(normalizedUsername));
    }

    /**
     * Update user fields from request
     */
    private void updateUserFields(User user, UserProfileRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }

        if (request.getCompany() != null) {
            user.setCompany(request.getCompany());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        if (request.getAccountType() != null) {
            user.setAccountType(request.getAccountType());
        }
    }

    /**
     * Build profile response DTO
     */
    private UserProfileResponse buildProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .dob(user.getDob())
                .company(user.getCompany())
                .location(user.getLocation())
                .profilePictureUrl(user.getProfilePictureUrl())
                .accountType(user.getAccountType())
                .isPortfolioCreated(user.getIsPortfolioCreated())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
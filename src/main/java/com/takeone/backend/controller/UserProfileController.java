package com.takeone.backend.controller;

import com.takeone.backend.dto.CheckUsernameRequest;
import com.takeone.backend.dto.CheckUsernameResponse;
import com.takeone.backend.dto.UserProfileRequest;
import com.takeone.backend.dto.UserProfileResponse;
import com.takeone.backend.security.UserPrincipal;
import com.takeone.backend.service.UserProfileService;
import com.takeone.backend.service.UsernameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UsernameService usernameService;

    /**
     * Check if username is available (unique)
     * Uses SHA-256 hash comparison with Redis caching
     */
    @PostMapping("/check-username")
    public ResponseEntity<CheckUsernameResponse> checkUsername(
            @Valid @RequestBody CheckUsernameRequest request
    ) {
        log.info("Checking username availability: {}", request.getUsername());

        boolean isAvailable = usernameService.isUsernameAvailable(request.getUsername());

        return ResponseEntity.ok(CheckUsernameResponse.builder()
                .username(request.getUsername())
                .available(isAvailable)
                .build());
    }

    /**
     * Get user profile
     * Returns complete user profile with cached data
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("Fetching profile for user: {}", currentUser.getUsername());

        UserProfileResponse profile = userProfileService.getUserProfile(currentUser.getId());

        return ResponseEntity.ok(profile);
    }

    /**
     * Create user profile (first time setup)
     * Called after initial Firebase authentication
     */
    @PostMapping("/profile")
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("Creating profile for user: {}", currentUser.getId());

        try {
            UserProfileResponse profile = userProfileService.createUserProfile(
                    currentUser.getId(),
                    request
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(profile);

        } catch (IllegalArgumentException e) {
            log.error("Profile creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Unexpected error during profile creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user profile
     * Updates existing profile information
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("Updating profile for user: {}", currentUser.getUsername());

        try {
            UserProfileResponse profile = userProfileService.updateUserProfile(
                    currentUser.getId(),
                    request
            );

            return ResponseEntity.ok(profile);

        } catch (IllegalArgumentException e) {
            log.error("Profile update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Unexpected error during profile update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
package com.takeone.backend.controller;

import com.takeone.backend.dto.ProfileRequest;
import com.takeone.backend.dto.UpdateProfileRequest;
import com.takeone.backend.model.User;
import com.takeone.backend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@RequestBody @Valid ProfileRequest request,
            @AuthenticationPrincipal User user) {
        try {
            User updatedUser = userProfileService.createProfile(user,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDob(),
                    request.getUsername(),
                    request.getMobile(),
                    request.getCompany(),
                    request.getLocation(),
                    request.getAccountType());
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        try {
            User updatedUser = userProfileService.updateProfile(
                    user,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDob(),
                    request.getUsername(),
                    request.getMobile(),
                    request.getCompany(),
                    request.getLocation(),
                    request.getAccountType(),
                    request.getIsPortfolioCreated());
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/username/check")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean available = userProfileService.isUsernameAvailable(username);
        if (available) {
            return ResponseEntity.ok("Username is available");
        } else {
            return ResponseEntity.status(409).body("Username is taken");
        }
    }
}

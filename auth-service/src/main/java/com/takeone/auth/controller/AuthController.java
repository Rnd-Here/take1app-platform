package com.takeone.auth.controller;

import com.takeone.auth.dto.*;
import com.takeone.auth.service.AuthService;
import com.takeone.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * Handles login, token refresh, and logout endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Main authentication endpoint
     * POST /api/auth/token
     * 
     * Flow:
     * 1. Verify Firebase token
     * 2. Get or create user
     * 3. Generate JWT tokens
     * 4. Create session
     * 5. Return tokens and user info
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(
            @Valid @RequestBody TokenRequest request) {
        
        log.info("Authentication request received for provider: {}", request.getProvider());
        
        // TODO: Implement authentication logic
        // 1. Call authService.authenticate(request)
        // 2. Return ApiResponse with AuthResponse
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Authentication successful")
        );
    }
    
    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Token refresh request received");
        
        // TODO: Implement token refresh logic
        // 1. Validate refresh token
        // 2. Generate new access token
        // 3. Return new tokens
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Token refreshed successfully")
        );
    }
    
    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LogoutRequest request) {
        
        log.info("Logout request received");
        
        // TODO: Implement logout logic
        // 1. Extract token from header
        // 2. Revoke refresh token
        // 3. Delete session
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Logged out successfully")
        );
    }
    
    /**
     * Send OTP for phone verification
     * POST /api/auth/send-otp
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpRequest request) {
        
        log.info("OTP request for phone: {}", request.getPhone());
        
        // TODO: Implement OTP sending logic
        // 1. Generate OTP
        // 2. Send via SMS/Firebase
        // 3. Store in database with expiry
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "OTP sent successfully")
        );
    }
    
    /**
     * Verify OTP
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        
        log.info("OTP verification for phone: {}", request.getPhone());
        
        // TODO: Implement OTP verification logic
        // 1. Verify OTP code
        // 2. Authenticate user
        // 3. Return tokens
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "OTP verified successfully")
        );
    }
    
    /**
     * Get current user context
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthContext>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Get current user context");
        
        // TODO: Implement get current user logic
        // 1. Extract and validate token
        // 2. Get user context
        // 3. Return user info and permissions
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "User context retrieved")
        );
    }
}

package com.takeone.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication Response
 * Returns session token and user details to UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    // Session information
    private String sessionToken;
    private LocalDateTime expiresAt;

    // User information
    private UserResponse user;

    // Error message (if any)
    private String error;
}
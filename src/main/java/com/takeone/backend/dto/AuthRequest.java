package com.takeone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Authentication Request
 * Sent from Flutter UI after Firebase authentication
 */
@Data
@Builder
@ToString(exclude = "idToken")
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Firebase token is required")
    private String idToken;
}
package com.takeone.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    
    private UserInfo user;
    
    // For manager context
    private UserInfo actingAs;
    private boolean isActingAsManager;
    
    // TODO: Add permissions map if needed
}

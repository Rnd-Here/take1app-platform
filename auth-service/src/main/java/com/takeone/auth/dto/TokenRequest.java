package com.takeone.auth.dto;

import com.takeone.auth.enums.AuthProvider;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequest {
    
    @NotNull(message = "Provider is required")
    private AuthProvider provider; // FIREBASE or SESSION
    
    @NotNull(message = "Token is required")
    private String token; // Firebase ID token or session token
    
    private String deviceInfo;
    
    // TODO: Add validation annotations
}

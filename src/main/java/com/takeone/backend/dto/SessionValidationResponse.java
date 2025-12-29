package com.takeone.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.takeone.backend.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionValidationResponse {

    private boolean valid;
    private String message;

    // User information (only included if valid)
    private Long userId;
    private String username;
    private String email;
    private AccountType accountType;

    // Session information (optional, for detailed response)
    private String deviceId;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
}
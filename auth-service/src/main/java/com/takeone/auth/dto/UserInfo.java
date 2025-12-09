package com.takeone.auth.dto;

import com.takeone.auth.enums.AccountType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    
    private Long userId;
    private String email;
    private String phone;
    private String displayName;
    private AccountType accountType;
    private boolean emailVerified;
    private boolean phoneVerified;
    
    // TODO: Add profile picture URL
}

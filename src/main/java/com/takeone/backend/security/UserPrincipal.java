package com.takeone.backend.security;

import com.takeone.backend.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User principal that holds authenticated user information
 * Available in SecurityContext after successful authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {

    private Long id;
    private String uid; // Firebase UID
    private String username;
    private String email;
    private AccountType accountType;

    public boolean isCreator() {
        return AccountType.CREATOR.equals(accountType);
    }

    public boolean isScout() {
        return AccountType.SCOUT.equals(accountType);
    }

    public boolean isFanatic() {
        return AccountType.FANATICS.equals(accountType);
    }

    public boolean isNewUser() {
        return AccountType.NEW_USER.equals(accountType);
    }
}
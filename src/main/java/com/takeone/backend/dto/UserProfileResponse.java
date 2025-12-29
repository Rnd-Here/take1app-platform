package com.takeone.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.takeone.backend.entity.AccountType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User Profile Response
 */
@Data
@Builder
@ToString(exclude = {"email", "mobile", "firstName", "lastName", "dob"})
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long id;
    private String uid;
    private String username;

    // Contact information (encrypted in DB)
    private String email;
    private String mobile;

    // Personal information (encrypted in DB)
    private String firstName;
    private String lastName;
    private String displayName;
    private String dob;

    // Professional information
    private String company;
    private String location;
    private String profilePictureUrl;

    // Account details
    private AccountType accountType;

    // Status flags
    private Boolean isPortfolioCreated;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Boolean isActive;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
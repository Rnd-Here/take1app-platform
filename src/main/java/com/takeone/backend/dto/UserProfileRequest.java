package com.takeone.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.takeone.backend.entity.AccountType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * User Profile Request (Create/Update)
 */
@Data
@Builder
@ToString(exclude = {"firstName", "lastName", "dob"})
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileRequest {

    // Username (required for first-time setup)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Username can only contain lowercase letters, numbers, and underscores")
    private String username;

    // Personal information
    @Size(max = 100, message = "First name is too long")
    private String firstName;

    @Size(max = 100, message = "Last name is too long")
    private String lastName;

    @Size(max = 100, message = "Display name is too long")
    private String displayName;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "DOB must be in format YYYY-MM-DD")
    private String dob;

    // Professional information
    @Size(max = 200, message = "Company name is too long")
    private String company;

    @Size(max = 200, message = "Location is too long")
    private String location;

    @Size(max = 1000, message = "Profile picture URL is too long")
    private String profilePictureUrl;

    // Account type
    private AccountType accountType;
}

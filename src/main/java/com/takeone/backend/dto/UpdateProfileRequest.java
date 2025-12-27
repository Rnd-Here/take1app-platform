package com.takeone.backend.dto;

import com.takeone.backend.model.AccountType;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String dob;
    private String username;
    private String mobile;
    private String company;
    private String location;
    private AccountType accountType;
    private Boolean isPortfolioCreated;
}

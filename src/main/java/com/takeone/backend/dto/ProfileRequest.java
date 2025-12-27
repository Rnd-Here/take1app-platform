package com.takeone.backend.dto;

import com.takeone.backend.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String dob;
    @NotBlank
    private String username;
    @NotBlank
    private String mobile;
    private String company;
    private String location;
    @NotNull
    private AccountType accountType;
}

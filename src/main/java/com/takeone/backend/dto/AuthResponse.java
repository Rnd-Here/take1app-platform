package com.takeone.backend.dto;

import com.takeone.backend.model.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String sessionToken;
    private User user;
}

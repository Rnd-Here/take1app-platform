package com.takeone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Check Username Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckUsernameResponse {
    private String username;
    private Boolean available;
}
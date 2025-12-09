package com.takeone.auth.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthContext {
    
    private Long actualUserId; // Manager's ID
    private Long effectiveUserId; // Talent's ID when acting as, otherwise same as actualUserId
    private boolean isActingAsManager;
    private Map<String, Object> permissions;
    
    // TODO: Add helper methods
    // public boolean hasPermission(String permission) { ... }
}

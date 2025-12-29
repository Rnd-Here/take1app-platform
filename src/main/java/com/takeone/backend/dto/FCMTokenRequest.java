package com.takeone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMTokenRequest {

    @NotBlank(message = "FCM Token is required")
    private String fcmToken;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Platform is required")
    private String platform; // ANDROID, IOS
}

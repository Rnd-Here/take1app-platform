package com.takeone.backend.controller;

import com.takeone.backend.dto.FCMTokenRequest;
import com.takeone.backend.security.UserPrincipal;
import com.takeone.backend.service.DeviceTokenService;
import com.takeone.backend.service.FirebaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final DeviceTokenService deviceTokenService;
    private final FirebaseService firebaseService;

    /**
     * Register or update FCM token for the authenticated user
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<?> registerFcmToken(
            @Valid @RequestBody FCMTokenRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Registering FCM token for user: {}", currentUser.getUsername());

        deviceTokenService.registerToken(
                currentUser.getId(),
                request.getFcmToken(),
                request.getDeviceId(),
                request.getPlatform());

        return ResponseEntity.ok().body(Map.of("message", "FCM token registered successfully"));
    }

    /**
     * Test endpoint to send a notification to the current user
     * Useful for verifying setup from the mobile app
     */
    @PostMapping("/test-push")
    public ResponseEntity<?> sendTestPush(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Sending test push to user: {}", currentUser.getUsername());

        Map<String, String> data = new HashMap<>();
        data.put("type", "test_notification");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        firebaseService.sendNotificationToUser(
                currentUser.getId(),
                "Hello " + currentUser.getUsername() + "!",
                "This is a test notification from Take One Backend.",
                data);

        return ResponseEntity.ok().body(Map.of("message", "Test notification triggered"));
    }
}

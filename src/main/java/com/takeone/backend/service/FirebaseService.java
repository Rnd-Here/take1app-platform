package com.takeone.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.*;
import com.takeone.backend.entity.DeviceToken;
import com.takeone.backend.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final DeviceTokenRepository deviceTokenRepository;

    public FirebaseToken verifyToken(String idToken) throws Exception {
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }

    /**
     * Send a notification to all active devices of a user
     */
    public void sendNotificationToUser(Long userId, String title, String body, Map<String, String> data) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (activeTokens.isEmpty()) {
            log.warn("No active push tokens found for user: {}", userId);
            return;
        }

        List<String> registrationTokens = activeTokens.stream()
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .addAllTokens(registrationTokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Successfully sent {} push notifications for user {}. Failures: {}",
                    response.getSuccessCount(), userId, response.getFailureCount());

            // Optional: Handle invalid tokens from BatchResponse if needed
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send a notification to a specific token
     */
    public void sendNotificationToToken(String fcmToken, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .setToken(fcmToken)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent push notification to token: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to token: {}", e.getMessage());
        }
    }
}

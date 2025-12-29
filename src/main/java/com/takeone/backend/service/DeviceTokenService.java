package com.takeone.backend.service;

import com.takeone.backend.entity.DeviceToken;
import com.takeone.backend.entity.User;
import com.takeone.backend.repository.DeviceTokenRepository;
import com.takeone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    /**
     * Register or update a device token (Upsert)
     */
    @Transactional
    public void registerToken(Long userId, String fcmToken, String deviceId, String platform) {
        log.info("Registering/Updating FCM token for user: {}, device: {}", userId, deviceId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId);

        if (existingToken.isPresent()) {
            DeviceToken token = existingToken.get();
            token.setFcmToken(fcmToken);
            token.setIsActive(true);
            token.setPlatform(platform);
            deviceTokenRepository.save(token);
            log.info("Updated existing FCM token for device: {}", deviceId);
        } else {
            // Check if this FCM token is already used by another record (and re-assign it)
            deviceTokenRepository.findByFcmToken(fcmToken).ifPresent(deviceTokenRepository::delete);

            DeviceToken token = DeviceToken.builder()
                    .user(user)
                    .fcmToken(fcmToken)
                    .deviceId(deviceId)
                    .platform(platform)
                    .isActive(true)
                    .build();
            deviceTokenRepository.save(token);
            log.info("Registered new FCM token for device: {}", deviceId);
        }
    }

    /**
     * Deactivate a specific device token (on logout)
     */
    @Transactional
    public void deactivateToken(Long userId, String deviceId) {
        log.info("Deactivating FCM token for user: {}, device: {}", userId, deviceId);
        deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresent(token -> {
                    token.setIsActive(false);
                    deviceTokenRepository.save(token);
                });
    }

    /**
     * Deactivate all device tokens for a user (on account deactivation)
     */
    @Transactional
    public void deactivateAllTokens(Long userId) {
        log.info("Deactivating all FCM tokens for user: {}", userId);
        deviceTokenRepository.findByUserIdAndIsActiveTrue(userId)
                .forEach(token -> {
                    token.setIsActive(false);
                    deviceTokenRepository.save(token);
                });
    }
}

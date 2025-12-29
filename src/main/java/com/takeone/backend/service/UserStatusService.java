package com.takeone.backend.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {

    private static final String STATUS_KEY_PREFIX = "user:status:";
    private static final long OFFLINE_THRESHOLD_MINUTES = 30;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Mark user as online
     */
    public void setUserOnline(Long userId) {
        String key = STATUS_KEY_PREFIX + userId;
        UserStatus status = UserStatus.builder()
                .userId(userId)
                .isOnline(true)
                .lastSeen(LocalDateTime.now())
                .build();

        redisTemplate.opsForValue().set(key, status);
        log.debug("User {} is now ONLINE", userId);
    }

    /**
     * Mark user as offline
     */
    public void setUserOffline(Long userId) {
        String key = STATUS_KEY_PREFIX + userId;
        Object val = redisTemplate.opsForValue().get(key);

        if (val instanceof UserStatus status) {
            status.setOnline(false);
            status.setLastSeen(LocalDateTime.now());
            // Keep status in Redis for a while after they go offline
            redisTemplate.opsForValue().set(key, status, OFFLINE_THRESHOLD_MINUTES, TimeUnit.MINUTES);
            log.debug("User {} is now OFFLINE", userId);
        }
    }

    /**
     * Check if a user is currently online
     */
    public boolean isUserOnline(Long userId) {
        String key = STATUS_KEY_PREFIX + userId;
        Object val = redisTemplate.opsForValue().get(key);

        if (val instanceof UserStatus status) {
            return status.isOnline();
        }
        return false;
    }

    /**
     * Get last seen time for a user
     */
    public LocalDateTime getLastSeen(Long userId) {
        String key = STATUS_KEY_PREFIX + userId;
        Object val = redisTemplate.opsForValue().get(key);

        if (val instanceof UserStatus status) {
            return status.getLastSeen();
        }
        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatus {
        private Long userId;
        private boolean isOnline;
        private LocalDateTime lastSeen;
    }
}

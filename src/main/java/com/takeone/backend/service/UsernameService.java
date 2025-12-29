package com.takeone.backend.service;

import com.takeone.backend.repository.UserRepository;
import com.takeone.backend.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsernameService {

    private static final String USERNAME_CACHE_PREFIX = "username:hash:";
    private static final long CACHE_TTL_HOURS = 24;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Check if username is available (unique)
     * Uses Redis cache for fast lookups
     *
     * @param username Username to check
     * @return true if available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // Normalize username (lowercase, trim)
        String normalizedUsername = username.toLowerCase().trim();

        // Generate SHA-256 hash
        String usernameHash = HashUtil.sha256(normalizedUsername);

        // Check Redis cache first
        String cacheKey = USERNAME_CACHE_PREFIX + usernameHash;
        Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);

        if (cachedResult != null) {
            log.debug("Username availability check - Cache HIT for: {}", username);
            return cachedResult;
        }

        // Cache miss - check database
        log.debug("Username availability check - Cache MISS for: {}", username);
        boolean isAvailable = !userRepository.existsByUsernameHash(usernameHash);

        // Store result in Redis cache
        redisTemplate.opsForValue().set(
                cacheKey,
                isAvailable,
                CACHE_TTL_HOURS,
                TimeUnit.HOURS);

        return isAvailable;
    }

    /**
     * Invalidate username cache when a username is taken
     * Called after user registration/username update
     */
    public void invalidateUsernameCache(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        String normalizedUsername = username.toLowerCase().trim();
        String usernameHash = HashUtil.sha256(normalizedUsername);
        String cacheKey = USERNAME_CACHE_PREFIX + usernameHash;

        redisTemplate.delete(cacheKey);
        log.debug("Invalidated username cache for: {}", username);
    }

    /**
     * Reserve username in cache temporarily (during registration process)
     * Prevents race conditions
     */
    public boolean reserveUsername(String username, Long userId, int durationMinutes) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String normalizedUsername = username.toLowerCase().trim();
        String usernameHash = HashUtil.sha256(normalizedUsername);
        String reservationKey = "username:reserve:" + usernameHash;

        // Try to set if not exists (NX flag)
        Boolean reserved = redisTemplate.opsForValue().setIfAbsent(
                reservationKey,
                userId,
                durationMinutes,
                TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(reserved)) {
            log.info("Username reserved: {} for user: {}", username, userId);
            return true;
        }

        // Check if already reserved by same user
        Object reservedBy = redisTemplate.opsForValue().get(reservationKey);
        if (reservedBy != null && reservedBy.equals(userId)) {
            log.debug("Username already reserved by same user: {}", userId);
            return true;
        }

        log.warn("Username reservation failed: {} (already reserved)", username);
        return false;
    }

    /**
     * Release username reservation
     */
    public void releaseUsernameReservation(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        String normalizedUsername = username.toLowerCase().trim();
        String usernameHash = HashUtil.sha256(normalizedUsername);
        String reservationKey = "username:reserve:" + usernameHash;

        redisTemplate.delete(reservationKey);
        log.debug("Released username reservation: {}", username);
    }
}

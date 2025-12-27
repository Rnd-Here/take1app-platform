package com.takeone.backend.service;

import com.takeone.backend.model.AccountType;
import com.takeone.backend.model.User;
import com.takeone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String USERNAME_HASH_PREFIX = "username:hash:";

    public User createProfile(User user, String firstName, String lastName, String dob, String username, String mobile,
            String company, String location, AccountType accountType) {
        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDob(dob);
        user.setUsername(username);
        user.setUsernameHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(username));

        user.setMobile(mobile);
        user.setCompany(company);
        user.setLocation(location);
        user.setAccountType(accountType);
        user.setIsPortfolioCreated(false);

        User savedUser = userRepository.save(user);
        cacheUsernameHash(savedUser.getUsernameHash(), savedUser.getUid());
        return savedUser;
    }

    public User updateProfile(User user, String firstName, String lastName, String dob, String username, String mobile,
            String company, String location, AccountType accountType,
            Boolean isPortfolioCreated) {
        if (username != null && !username.equals(user.getUsername())) {
            if (!isUsernameAvailable(username)) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(username);
            user.setUsernameHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(username));
            cacheUsernameHash(user.getUsernameHash(), user.getUid());
        }

        if (firstName != null)
            user.setFirstName(firstName);
        if (lastName != null)
            user.setLastName(lastName);
        if (dob != null)
            user.setDob(dob);
        if (mobile != null)
            user.setMobile(mobile);
        if (company != null)
            user.setCompany(company);
        if (location != null)
            user.setLocation(location);
        if (accountType != null)
            user.setAccountType(accountType);
        if (isPortfolioCreated != null && user.getAccountType() == AccountType.CREATOR) {
            user.setIsPortfolioCreated(isPortfolioCreated);
        }

        return userRepository.save(user);
    }

    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty())
            return false;

        String hash = DigestUtils.sha256Hex(username);
        String key = USERNAME_HASH_PREFIX + hash;

        // Check Redis
        Boolean needsCheck = redisTemplate.hasKey(key);
        if (needsCheck) {
            return false; // Taken
        }

        // Check DB
        // Fetch the user so we can cache the mapping (Hash -> UID)
        var userOpt = userRepository.findByUsernameHash(hash);
        if (userOpt.isPresent()) {
            cacheUsernameHash(hash, userOpt.get().getUid());
            return false;
        }

        return true;
    }

    private void cacheUsernameHash(String hash, String uid) {
        String key = USERNAME_HASH_PREFIX + hash;
        // Store in Redis with TTL (LRU approximation by expiration/eviction)
        // Store for 7 days
        redisTemplate.opsForValue().set(key, uid, 7, TimeUnit.DAYS);
    }
}

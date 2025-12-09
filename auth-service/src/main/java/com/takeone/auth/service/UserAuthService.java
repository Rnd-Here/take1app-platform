package com.takeone.auth.service;

import com.takeone.auth.dto.UserInfo;
import com.takeone.auth.model.User;
import com.takeone.auth.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Authentication Service
 * Handles user creation and retrieval from Firebase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {
    
    private final UserRepository userRepository;
    
    /**
     * Get existing user or create new one from Firebase token
     */
    @Transactional
    public UserInfo getOrCreateUserFromFirebase(FirebaseToken firebaseToken) {
        log.info("Getting or creating user for Firebase UID: {}", firebaseToken.getUid());
        
        // TODO: Implement user retrieval/creation
        // 1. Check if user exists by Firebase UID
        // 2. If exists, return user info
        // 3. If not exists:
        //    a. Create new User entity
        //    b. Set Firebase UID
        //    c. Set email/phone from token
        //    d. Set default account type (TALENT)
        //    e. Save to database
        // 4. Map to UserInfo DTO
        // 5. Return UserInfo
        
        return null; // Placeholder
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        // TODO: Implement get user by ID
        // 1. Find user in repository
        // 2. Throw exception if not found
        // 3. Return user
        
        return null; // Placeholder
    }
    
    /**
     * Get user info (DTO) by ID
     */
    public UserInfo getUserInfo(Long userId) {
        // TODO: Implement get user info
        // 1. Get user entity
        // 2. Map to UserInfo DTO
        // 3. Return DTO
        
        return null; // Placeholder
    }
}

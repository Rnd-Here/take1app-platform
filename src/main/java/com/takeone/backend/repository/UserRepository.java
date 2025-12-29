package com.takeone.backend.repository;

import com.takeone.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Firebase UID
     */
    Optional<User> findByUid(String uid);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email (encrypted, so this needs special handling)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if Firebase UID exists
     */
    boolean existsByUid(String uid);

    boolean findByUsernameHash(String hash);

    boolean existsByUsernameHash(String usernameHash);
}
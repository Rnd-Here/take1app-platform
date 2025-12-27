package com.takeone.backend.repository;

import com.takeone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String uid);

    Optional<User> findByUsernameHash(String usernameHash);
}

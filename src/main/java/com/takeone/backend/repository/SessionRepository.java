package com.takeone.backend.repository;

import com.takeone.backend.model.Session;
import com.takeone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshToken(String refreshToken);

    List<Session> findByUserAndIsActiveTrue(User user);

    void deleteByRefreshToken(String refreshToken);
}

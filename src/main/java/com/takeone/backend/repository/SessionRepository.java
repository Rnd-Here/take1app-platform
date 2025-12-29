package com.takeone.backend.repository;

import com.takeone.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * Find active session by refresh token with User eagerly loaded
     */
    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.refreshToken = :refreshToken AND s.isActive = true")
    Optional<Session> findByRefreshTokenAndIsActiveTrue(@Param("refreshToken") String refreshToken);

    /**
     * Find all active sessions for a user
     */
    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.isActive = true")
    List<Session> findActiveSessionsByUserId(@Param("userId") Long userId);

    /**
     * Deactivate all sessions for a user
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.user.id = :userId AND s.isActive = true")
    int deactivateAllUserSessions(@Param("userId") Long userId);

    /**
     * Deactivate expired sessions
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.expiresAt < :now AND s.isActive = true")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Find session by token (active or inactive)
     */
    Optional<Session> findByRefreshToken(String refreshToken);

    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.user.id = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") Long userId);

    /**
     * Delete old inactive sessions (for cleanup)
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.isActive = false AND s.lastAccessedAt < :cutoffDate")
    int deleteOldInactiveSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
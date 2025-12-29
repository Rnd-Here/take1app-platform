package com.takeone.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_refresh_token", columnList = "refresh_token"),
                @Index(name = "idx_user_active", columnList = "user_id, is_active"),
                @Index(name = "idx_expires_at", columnList = "expires_at"),
                @Index(name = "idx_last_accessed", columnList = "last_accessed_at")
        }
)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to User with proper relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_user")
    )
    private User user;

    // Session token - unique identifier
    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    // Device identification
    @Column(name = "device_id", length = 500)
    private String deviceId;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    // Session validity
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    // Helper method to check if session is expired
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Helper method to check if session is valid
    @Transient
    public boolean isValid() {
        return isActive && !isExpired();
    }
}
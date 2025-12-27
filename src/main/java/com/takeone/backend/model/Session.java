package com.takeone.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @lombok.ToString.Exclude
    private User user;

    @Column(nullable = false, unique = true)
    private String refreshToken;

    private String deviceId; // Or User-Agent

    private String ipAddress;

    private LocalDateTime expiresAt;

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Session session = (Session) o;
        return id != null && id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

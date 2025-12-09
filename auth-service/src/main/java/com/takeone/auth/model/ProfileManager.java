package com.takeone.auth.model;

import com.takeone.auth.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_managers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileManager {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "manager_user_id", nullable = false)
    private Long managerUserId;
    
    @Column(name = "talent_user_id", nullable = false)
    private Long talentUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType;
    
    @Column(columnDefinition = "TEXT")
    private String permissions; // JSON string with permissions
    
    private String status; // ACTIVE, PENDING, REVOKED
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime approvedAt;
    private LocalDateTime revokedAt;
    
    // TODO: Add fields
    // - invitationToken
    // - expiresAt
}

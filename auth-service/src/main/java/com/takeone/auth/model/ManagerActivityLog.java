package com.takeone.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "manager_activity_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagerActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long managerUserId;
    private Long talentUserId;
    private String action;
    private String resourceType;
    private Long resourceId;
    private LocalDateTime createdAt;
    
    // TODO: Add JSON details field
}

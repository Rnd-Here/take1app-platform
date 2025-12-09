package com.takeone.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "manager_active_contexts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ManagerActiveContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long managerUserId;
    private Long actingAsUserId;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    
    // TODO: Store session info
}

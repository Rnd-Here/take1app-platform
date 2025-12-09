package com.takeone.auth.model;

import com.takeone.auth.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private String deviceId;
    private String deviceName;
    
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    
    private String fcmToken; // For push notifications
    private LocalDateTime lastActiveAt;
    
    // TODO: Store device metadata
}

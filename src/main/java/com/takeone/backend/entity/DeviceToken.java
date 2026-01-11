package com.takeone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_device_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fcm_token", columnNames = "fcm_token"),
        @UniqueConstraint(name = "uk_user_device", columnNames = { "user_id", "device_id" })
})
@DynamicUpdate // Efficient for toggling isActive flag
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "device_id", nullable = false, length = 500)
    private String deviceId;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // e.g., ANDROID, IOS

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "language")
    private String language;

    @UpdateTimestamp

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
}

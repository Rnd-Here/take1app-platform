package com.takeone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_uid", columnList = "uid"),
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_username_hash", columnList = "username_hash"),
        @Index(name = "idx_account_type", columnList = "account_type"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Getter
@Setter
@ToString(exclude = {"email", "mobile", "firstName", "lastName", "dob"})
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Firebase UID - Unique identifier from Firebase
    @Column(name = "uid", unique = true, nullable = false, length = 128)
    private String uid;

    // Username - Unique, case-sensitive
    @Column(name = "username", unique = true, length = 50)
    private String username;

    // Username Hash - SHA-256 for quick uniqueness check
    @Column(name = "username_hash", unique = true, length = 64)
    private String usernameHash;

    // Encrypted sensitive fields
    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    @Column(name = "email", length = 500)
    private String email;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    @Column(name = "mobile", length = 500)
    private String mobile;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    @Column(name = "first_name", length = 500)
    private String firstName;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    @Column(name = "last_name", length = 500)
    private String lastName;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    @Column(name = "dob", length = 500) // Format: YYYY-MM-DD encrypted
    private String dob;

    // Public profile fields (not encrypted)
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "company", length = 200)
    private String company;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "profile_picture_url", length = 1000)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType = AccountType.NEW_USER;

    // Profile completion and verification flags
    @Column(name = "is_portfolio_created", nullable = false)
    private Boolean isPortfolioCreated = false;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Timestamps
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to set username and automatically generate hash
    public void setUsernameWithHash(String username) {
        this.username = username;
        this.usernameHash = com.takeone.backend.util.HashUtil.sha256(username);
    }
}
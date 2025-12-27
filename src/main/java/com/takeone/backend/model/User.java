package com.takeone.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid; // Firebase UID

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String usernameHash; // SHA-256 of username

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    private String email;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    private String mobile;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    private String firstName;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    private String lastName;

    @Convert(converter = com.takeone.backend.util.AttributeEncryptor.class)
    private String dob; // Stored as String for encryption, format YYYY-MM-DD

    private String displayName;

    private String company;

    private String location;

    @Column(length = 1000)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private Boolean isPortfolioCreated = false;

    private Boolean isEmailVerified = false;

    private Boolean isPhoneVerified = false;

    private Boolean isActive = true;

    private LocalDateTime lastLogin;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Session> sessions = new java.util.ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

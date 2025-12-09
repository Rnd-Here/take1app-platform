package com.takeone.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.credentials.path}")
    private String credentialsPath;
    
    @PostConstruct
    public void initialize() {
        
        // TODO: Initialize Firebase Admin SDK
        // 1. Load credentials from file
        // 2. Build FirebaseOptions
        // 3. Initialize FirebaseApp
        // 4. Handle exceptions
        
        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✓ Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }
}

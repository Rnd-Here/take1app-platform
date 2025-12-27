package com.takeone.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;
    private final ResourceLoader resourceLoader;

    public FirebaseConfig(FirebaseProperties firebaseProperties, ResourceLoader resourceLoader) {
        this.firebaseProperties = firebaseProperties;
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // Priority 1: Base64 Env Var (Production)
            if (StringUtils.hasText(firebaseProperties.getBase64())) {
                try (InputStream serviceAccount = new ByteArrayInputStream(
                        Base64.getDecoder().decode(firebaseProperties.getBase64()))) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    return FirebaseApp.initializeApp(options);
                }
            }

            // Priority 2: File Path (Local Dev)
            Resource resource = resourceLoader.getResource(firebaseProperties.getPath());
            if (resource.exists()) {
                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    return FirebaseApp.initializeApp(options);
                }
            } else {
                throw new IOException(
                        "Firebase configuration not found. Please set app.firebase.config.base64 or ensure file exists at "
                                + firebaseProperties.getPath());
            }
        }
        return FirebaseApp.getInstance();
    }
}

package com.takeone.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.firebase.config")
public class FirebaseProperties {
    /**
     * Path to the service account JSON file. Can be absolute or classpath relative.
     */
    private String path = "classpath:firebase-service-account.json";

    /**
     * Base64 encoded service account JSON. If present, usage takes priority over
     * path.
     */
    private String base64;
}

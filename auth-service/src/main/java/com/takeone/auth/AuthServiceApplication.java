package com.takeone.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * TakeOne Auth Service - Main Application
 * Handles authentication, authorization, and token management
 */
@SpringBootApplication(scanBasePackages = {"com.takeone.auth", "com.takeone.common"})
@EnableFeignClients
@EnableJpaAuditing
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("🎬 TakeOne Auth Service Started - Where Every Take Matters!");
    }
}

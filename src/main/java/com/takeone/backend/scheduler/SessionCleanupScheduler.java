package com.takeone.backend.scheduler;

import com.takeone.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to clean up expired sessions periodically
 * Runs daily at 3 AM
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    /**
     * Clean up expired sessions daily
     * Cron: 0 0 3 * * * = 3:00 AM every day
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredSessions() {
        log.info("Starting scheduled session cleanup");
        try {
            sessionService.cleanupExpiredSessions();
            log.info("Completed scheduled session cleanup");
        } catch (Exception e) {
            log.error("Error during scheduled session cleanup: {}", e.getMessage(), e);
        }
    }
}
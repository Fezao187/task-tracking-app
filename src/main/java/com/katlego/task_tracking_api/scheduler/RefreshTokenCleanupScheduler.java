package com.katlego.task_tracking_api.scheduler;

import com.katlego.task_tracking_api.security.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupScheduler(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "${schedule.refresh-token-cleanup-schedule-time}")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");

        try {
            int deletedCount = refreshTokenService.deleteExpiredTokens();
            log.info("Deleted {} expired refresh tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during refresh token cleanup", e);
        }
    }
}
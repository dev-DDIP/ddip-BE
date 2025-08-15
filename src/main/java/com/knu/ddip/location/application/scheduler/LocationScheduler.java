package com.knu.ddip.location.application.scheduler;

import com.knu.ddip.location.infrastructure.repositoroy.redis.RedisLocationExpiryCleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationScheduler {

    private final RedisLocationExpiryCleanup cleanup;

    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void cleanupExpiredUserLocations() {
        long now = System.currentTimeMillis();
        cleanup.cleanupExpiredUserLocations(now);
    }

}
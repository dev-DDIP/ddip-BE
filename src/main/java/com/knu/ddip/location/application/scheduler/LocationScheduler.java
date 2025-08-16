package com.knu.ddip.location.application.scheduler;

import com.knu.ddip.location.application.service.LocationWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationScheduler {

    private final LocationWriter locationWriter;

    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void cleanupExpiredUserLocations() {
        long now = System.currentTimeMillis();
        locationWriter.cleanupExpiredUserLocations(now);
    }

}
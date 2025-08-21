package com.knu.ddip.location.application.scheduler;

import com.knu.ddip.location.application.service.LocationWriter;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationScheduler {

    private final LocationWriter locationWriter;

    @SchedulerLock(
            name = "cleanup_locations_lock",
            lockAtLeastFor = "30s",
            lockAtMostFor = "5m"
    )
    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void cleanupExpiredUserLocations() {
        long now = System.currentTimeMillis();
        locationWriter.cleanupExpiredUserLocations(now);
    }

}
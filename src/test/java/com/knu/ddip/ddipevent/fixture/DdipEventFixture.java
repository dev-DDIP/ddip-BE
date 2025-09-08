package com.knu.ddip.ddipevent.fixture;

import com.knu.ddip.ddipevent.domain.DdipStatus;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;

import java.time.Instant;
import java.util.UUID;

public abstract class DdipEventFixture {

    public static DdipEventEntity createDdipEvent() {
        DdipEventEntity event = createDdipEvent(0.0, 0.0, DdipStatus.OPEN, "content", "cellId");
        return event;
    }

    public static DdipEventEntity createDdipEvent(Double lat, Double lon, DdipStatus status, String content, String cellId) {
        DdipEventEntity event = DdipEventEntity.builder()
                .content(content)
                .createdAt(Instant.now())
                .difficulty(1)
                .latitude(lat)
                .longitude(lon)
                .requesterId(UUID.randomUUID())
                .reward(1)
                .status(status)
                .title("title")
                .build();
        return event;
    }
}

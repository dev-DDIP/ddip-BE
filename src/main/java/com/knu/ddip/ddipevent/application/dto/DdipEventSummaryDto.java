package com.knu.ddip.ddipevent.application.dto;

import com.knu.ddip.ddipevent.domain.DdipStatus;

public record DdipEventSummaryDto(
        String id,
        String title,
        Integer reward,
        Double latitude,
        Double longitude,
        DdipStatus status,
        String requesterId,
        String createdAt,
        Integer applicantCount,
        String content,
        Double distance,
        Integer difficulty
) {
}

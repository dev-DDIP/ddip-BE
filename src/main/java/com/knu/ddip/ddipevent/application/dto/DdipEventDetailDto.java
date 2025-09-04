package com.knu.ddip.ddipevent.application.dto;

import com.knu.ddip.ddipevent.domain.DdipStatus;

import java.util.List;

public record DdipEventDetailDto(
        String id,
        String title,
        String content,
        Integer reward,
        Double latitude,
        Double longitude,
        DdipStatus status,
        String createdAt,
        List<UserSummaryDto> applicants,
        UserSummaryDto selectedResponder,
        List<PhotoDto> photos,
        List<InteractionDto> interactions
) {
}

package com.knu.ddip.location.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetNeighborsRequest(
        UUID userId, // 테스트용 파라미터
        double lat,
        double lng
) {
    public static GetNeighborsRequest of(UUID userId, double lat, double lng) {
        return GetNeighborsRequest.builder()
                .userId(userId)
                .lat(lat)
                .lng(lng)
                .build();
    }
}

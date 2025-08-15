package com.knu.ddip.location.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateMyLocationRequest(
        UUID userId, // 테스트용 파라미터
        double lat,
        double lng
) {
    public static UpdateMyLocationRequest of(UUID userId, double lat, double lng) {
        return new UpdateMyLocationRequest(userId, lat, lng);
    }
}

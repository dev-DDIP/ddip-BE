package com.knu.ddip.location.application.dto;

import lombok.Builder;

@Builder
public record UpdateMyLocationRequest(
        double lat,
        double lng
) {
    public static UpdateMyLocationRequest of(double lat, double lng) {
        return new UpdateMyLocationRequest(lat, lng);
    }
}

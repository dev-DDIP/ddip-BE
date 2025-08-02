package com.knu.ddip.auth.business.dto;

public record JwtRefreshRequest(
        String refreshToken,
        String deviceType
) {
}

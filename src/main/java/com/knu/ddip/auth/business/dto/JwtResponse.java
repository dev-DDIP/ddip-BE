package com.knu.ddip.auth.business.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}

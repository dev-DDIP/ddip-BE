package com.knu.ddip.auth.business.dto;

import com.knu.ddip.auth.domain.OAuthToken;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record OAuthTokenDto(
        String accessToken,
        String refreshToken,
        long expiresIn) {

    public static OAuthTokenDto from(OAuthToken oAuthToken) {
        return OAuthTokenDto.builder()
                .accessToken(oAuthToken.getAccessToken())
                .refreshToken(oAuthToken.getRefreshToken())
                .expiresIn(oAuthToken.getExpiresIn())
                .build();
    }
}

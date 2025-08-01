package com.knu.ddip.auth.business.dto;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PROTECTED)
public record OAuthLoginResponse(
        String accessToken,
        String refreshToken,
        UUID OAuthMappingEntityId,
        boolean needRegister
) {
    public static OAuthLoginResponse toJwt(String accessToken, String refreshToken) {
        return OAuthLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .needRegister(false)
                .build();
    }

    public static OAuthLoginResponse toSignUp(UUID OAuthMappingEntityId) {
        return OAuthLoginResponse.builder()
                .OAuthMappingEntityId(OAuthMappingEntityId)
                .needRegister(true)
                .build();
    }
}

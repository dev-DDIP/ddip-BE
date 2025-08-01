package com.knu.ddip.auth.domain;

import com.knu.ddip.auth.exception.OAuthErrorException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthToken {
    private final OAuthProvider provider;
    private final String accessToken;
    private final String refreshToken;
    private final Long expiresIn;
    private final LocalDateTime issuedAt;

    public static OAuthToken ofKakao(String accessToken, String refreshToken, Long expiresIn) {
        if (accessToken == null) {
            throw new OAuthErrorException("응답이 올바르지 않아 accessToken이 전달되지 않았습니다.");
        }

        return OAuthToken.builder()
                .provider(OAuthProvider.KAKAO)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public static OAuthToken create(OAuthProvider provider, String accessToken, String refreshToken,
                                    Long expiresIn) {

        if (accessToken == null) {
            return null;
        }

        return OAuthToken.builder()
                .provider(provider)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}

package com.knu.ddip.auth.business.service.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.auth.domain.OAuthToken;
import com.knu.ddip.auth.domain.OAuthUserInfo;

public record KakaoUserInfoResponse(
        String id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public OAuthUserInfo toDomain(OAuthProvider provider, OAuthToken oAuthToken) {
        String email = kakaoAccount != null ? kakaoAccount.email : null;
        String nickname = kakaoAccount != null && kakaoAccount.profile != null
                ? kakaoAccount.profile.nickname : null;

        return OAuthUserInfo.create(id, email, nickname, provider, oAuthToken);
    }

    public record KakaoAccount(
            String email,
            Profile profile
    ) {
        public record Profile(
                String nickname
        ) {
        }
    }
}

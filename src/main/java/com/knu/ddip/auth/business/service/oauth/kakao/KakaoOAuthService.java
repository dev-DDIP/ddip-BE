package com.knu.ddip.auth.business.service.oauth.kakao;

import com.knu.ddip.auth.business.service.oauth.OAuthService;
import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.auth.domain.OAuthToken;
import com.knu.ddip.auth.domain.OAuthUserInfo;
import com.knu.ddip.auth.exception.OAuthErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private static final String KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_AUTHORIZE_URI = "https://kauth.kakao.com/oauth/authorize";
    private final RestTemplate restTemplate;

    @Value("${KAKAO_REST_API_KEY}")
    private String clientId;

    @Value("${KAKAO_BACKEND_REDIRECT_URI}")
    private String redirectUri;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        OAuthToken oauthToken = getOAuthToken(code);

        return getUserInfoByToken(oauthToken);
    }

    @Override
    public String getRedirectUrl(String state) {

        return KAKAO_AUTHORIZE_URI
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&state=" + state;
    }

    @Override
    public boolean isBackendRedirect() {
        return true;
    }

    private OAuthToken getOAuthToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    KAKAO_TOKEN_URI,
                    HttpMethod.POST,
                    request,
                    KakaoTokenResponse.class
            );

            KakaoTokenResponse kakaoTokenResponse = response.getBody();

            return kakaoTokenResponse.toDomain();

        } catch (Exception e) {
            throw new OAuthErrorException("카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private OAuthUserInfo getUserInfoByToken(OAuthToken oauthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oauthToken.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URI,
                    HttpMethod.GET,
                    request,
                    KakaoUserInfoResponse.class
            );

            KakaoUserInfoResponse userInfoResponse = response.getBody();

            return userInfoResponse.toDomain(OAuthProvider.KAKAO, oauthToken);

        } catch (Exception e) {
            throw new OAuthErrorException("카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

}

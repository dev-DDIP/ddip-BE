package com.knu.ddip.auth.business.service.oauth;

import com.knu.ddip.auth.domain.OAuthUserInfo;

public interface OAuthService {
    OAuthUserInfo getUserInfo(String code);

    String getRedirectUrl(String state);

    boolean isBackendRedirect();
}

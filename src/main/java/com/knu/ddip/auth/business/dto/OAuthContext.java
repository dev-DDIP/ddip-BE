package com.knu.ddip.auth.business.dto;

import com.knu.ddip.auth.domain.DeviceType;
import com.knu.ddip.auth.domain.OAuthProvider;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record OAuthContext(OAuthProvider oAuthProvider, DeviceType deviceType) {
    public static OAuthContext of(OAuthProvider oAuthProvider, DeviceType deviceType) {
        return OAuthContext.builder()
                .oAuthProvider(oAuthProvider)
                .deviceType(deviceType)
                .build();
    }
}

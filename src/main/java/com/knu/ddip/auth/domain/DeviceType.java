package com.knu.ddip.auth.domain;

import com.knu.ddip.auth.exception.OAuthBadRequestException;

public enum DeviceType {
    TABLET, PHONE;

    public static DeviceType fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new OAuthBadRequestException("state값이 없습니다.");
        }

        try {
            return DeviceType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OAuthBadRequestException("올바른 DeviceType이 아닙니다.");
        }
    }
}

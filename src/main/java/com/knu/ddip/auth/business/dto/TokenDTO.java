package com.knu.ddip.auth.business.dto;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record TokenDTO(
        String value
) {
    public static TokenDTO from(String value) {
        return TokenDTO.builder().value(value).build();
    }
}

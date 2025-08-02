package com.knu.ddip.auth.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

//TODO: 추후 email, role 등 추가 고려
@Getter
@Builder
public class AuthUser {
    private UUID id;

    public static AuthUser from(Token token) {
        return new AuthUser(UUID.fromString(token.getSubject()));
    }
}

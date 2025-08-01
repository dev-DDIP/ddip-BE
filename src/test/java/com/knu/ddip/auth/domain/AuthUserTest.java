package com.knu.ddip.auth.domain;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserTest {

    @Test
    public void from_whenValidToken_returnAuthUser() {
        //Given
        UUID userId = UUID.randomUUID();
        Token token = Token.of(TokenType.ACCESS, "token-value", userId.toString(), new Date(),
                new Date());

        //When
        AuthUser authUser = AuthUser.from(token);

        //Then
        assertThat(authUser.getId()).isEqualTo(userId);
    }

    @Test
    public void builder_whenValidInput_returnAuthUser() {
        //Given
        UUID userId = UUID.randomUUID();

        //When
        AuthUser authUser = AuthUser.builder()
                .id(userId)
                .build();

        //Then
        assertThat(authUser.getId()).isEqualTo(userId);
    }
}

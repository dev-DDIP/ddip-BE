package com.knu.ddip.user.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDomainTest {

    @Test
    public void create_returnActiveUser() {
        //Given
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String nickName = "testName";

        //When
        UserDomain userDomain = UserDomain.create(id, email, nickName, "ACTIVE");

        //Then
        assertEquals(id, userDomain.getId());
        assertEquals(email, userDomain.getEmail());
    }
}

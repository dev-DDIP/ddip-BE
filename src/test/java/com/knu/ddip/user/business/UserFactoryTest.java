package com.knu.ddip.user.business;

import com.knu.ddip.user.business.service.UserFactory;
import com.knu.ddip.user.domain.User;
import com.knu.ddip.user.domain.UserDomain;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class UserFactoryTest {

    @Test
    public void create_whenStatusIsActive_returnActiveUser() {
        //Given
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String status = "ACTIVE";
        String nickname = "testName";

        //When
        User user = UserFactory.create(id, email, nickname, status);

        //Then
        assertInstanceOf(UserDomain.class, user);
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
    }
}

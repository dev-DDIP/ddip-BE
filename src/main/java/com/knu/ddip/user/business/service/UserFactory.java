package com.knu.ddip.user.business.service;

import com.knu.ddip.user.domain.User;
import com.knu.ddip.user.domain.UserDomain;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFactory {

    public static User create(UUID id, String email, String nickname, String status) {
        return UserDomain.create(id, email, nickname, status);
    }
}

package com.knu.ddip.user.business.dto;

import com.knu.ddip.user.business.service.UserFactory;
import com.knu.ddip.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntityDto {
    private final UUID id;
    private final String email;
    private final String nickname;
    private final String status;

    public static UserEntityDto create(UUID id, String email, String nickname, String status) {
        return UserEntityDto.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .status(status)
                .build();
    }

    public static UserEntityDto create(UUID id, String email, String status) {
        return UserEntityDto.builder()
                .id(id)
                .email(email)
                .status(status)
                .build();
    }

    public User toDomain() {
        return UserFactory.create(id, email, nickname, status);
    }
}

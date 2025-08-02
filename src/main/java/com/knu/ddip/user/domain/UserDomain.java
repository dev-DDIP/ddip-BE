package com.knu.ddip.user.domain;

import com.knu.ddip.user.business.dto.UserEntityDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

import static com.knu.ddip.user.domain.UserStatus.INACTIVE;
import static com.knu.ddip.user.domain.UserStatus.WITHDRAWN;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDomain implements User {

    private final UUID id;
    private final String email;
    private final String nickname;
    private final String status;

    public static UserDomain create(UUID id, String email, String nickname, String status) {
        return new UserDomain(id, email, nickname, status);
    }

    @Override
    public UserEntityDto toEntityDto() {
        return UserEntityDto.create(id, email, nickname, status);
    }

    @Override
    public boolean isWithdrawn() {
        return this.status.equals(WITHDRAWN.name());
    }

    @Override
    public boolean isInactive() {
        return this.status.equals(INACTIVE.name());
    }
}

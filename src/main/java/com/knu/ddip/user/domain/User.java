package com.knu.ddip.user.domain;

import com.knu.ddip.user.business.dto.UserEntityDto;

import java.util.UUID;

public interface User {
    UserEntityDto toEntityDto();

    UUID getId();

    String getEmail();

    String getNickname();

    boolean isWithdrawn();

    boolean isInactive();
}

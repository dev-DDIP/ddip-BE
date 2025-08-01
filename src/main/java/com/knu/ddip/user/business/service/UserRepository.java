package com.knu.ddip.user.business.service;

import com.knu.ddip.user.business.dto.UserEntityDto;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    UserEntityDto save(String email, String nickName, String status);

    void update(UserEntityDto userEntityDto);

    void delete(UUID userId);

    UserEntityDto getByEmail(String email);

    Optional<UserEntityDto> findOptionalByEmail(String email);

    UserEntityDto getById(UUID userId);
}

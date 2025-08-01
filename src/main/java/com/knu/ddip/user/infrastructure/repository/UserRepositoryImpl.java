package com.knu.ddip.user.infrastructure.repository;

import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.business.service.UserRepository;
import com.knu.ddip.user.exception.UserNotFoundException;
import com.knu.ddip.user.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserEntityDto save(String email, String nickName, String status) {
        UserEntity user = UserEntity.create(email, nickName, status);
        UserEntity savedUser = userJpaRepository.save(user);
        return savedUser.toEntityDto();
    }

    @Override
    public void update(UserEntityDto userEntityDto) {
        UserEntity user = userJpaRepository.findById(userEntityDto.getId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.update(userEntityDto);
    }

    @Override
    public void delete(UUID userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        userJpaRepository.delete(user);
    }

    public UserEntityDto getByEmail(String email) {
        UserEntity user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return user.toEntityDto();
    }

    @Override
    public Optional<UserEntityDto> findOptionalByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::toEntityDto);
    }

    @Override
    public UserEntityDto getById(UUID userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return user.toEntityDto();
    }
}

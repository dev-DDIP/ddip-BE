package com.knu.ddip.user.business.service;

import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.auth.business.service.OAuthLoginService;
import com.knu.ddip.auth.domain.DeviceType;
import com.knu.ddip.auth.exception.OAuthBadRequestException;
import com.knu.ddip.user.business.dto.DummyRequest;
import com.knu.ddip.user.business.dto.SignupRequest;
import com.knu.ddip.user.business.dto.UniqueMailResponse;
import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.domain.User;
import com.knu.ddip.user.domain.UserStatus;
import com.knu.ddip.user.exception.UserEmailDuplicateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OAuthLoginService oAuthLoginService;

    public User getUserByEmail(String email) {
        UserEntityDto userEntityDto = userRepository.getByEmail(email);
        return userEntityDto.toDomain();
    }

    public UniqueMailResponse checkEmailUniqueness(String email) {
        Optional<UserEntityDto> optionalUser = userRepository.findOptionalByEmail(email);

        if (optionalUser.isEmpty()) {
            return UniqueMailResponse.ofUnique();
        }

        UserEntityDto userEntityDto = optionalUser.get();
        User user = userEntityDto.toDomain();

        if (user.isWithdrawn()) {
            return UniqueMailResponse.ofWithDrawn();
        } else if (user.isInactive()) {
            return UniqueMailResponse.ofInActive();
        } else {
            return UniqueMailResponse.ofDuplicate();
        }
    }

    @Transactional
    public JwtResponse signUp(SignupRequest request) {
        UniqueMailResponse uniqueCheck = checkEmailUniqueness(request.email());
        if (!uniqueCheck.isUnique()) {
            throw new UserEmailDuplicateException(uniqueCheck.message());
        }

        UserEntityDto userEntityDto = userRepository.save(request.email(), request.nickname(),
                UserStatus.ACTIVE.name());
        User user = userEntityDto.toDomain();

        try {
            return oAuthLoginService.linkOAuthWithUser(
                    user,
                    request.oAuthMappingEntityId(),
                    request.provider(),
                    DeviceType.fromString(request.deviceType())
            );
        } catch (OAuthBadRequestException e) {
            userRepository.delete(user.getId());
            throw e;
        }
    }

    @Transactional
    public JwtResponse dummyLogin(DummyRequest dummyRequest) {
        UserEntityDto userEntityDto = userRepository.findOptionalByEmail(dummyRequest.email())
                .map(user -> userRepository.getByEmail(dummyRequest.email()))
                .orElseGet(() -> userRepository.save(
                        dummyRequest.email(),
                        dummyRequest.nickname(),
                        UserStatus.ACTIVE.name()
                ));
        User user = userEntityDto.toDomain();

        return oAuthLoginService.generateTokensForUser(user.getId(), DeviceType.PHONE);
    }
}

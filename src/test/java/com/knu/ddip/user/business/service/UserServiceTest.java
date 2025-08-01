package com.knu.ddip.user.business.service;

import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.auth.business.service.OAuthLoginService;
import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.user.business.dto.SignupRequest;
import com.knu.ddip.user.business.dto.UniqueMailResponse;
import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.domain.User;
import com.knu.ddip.user.exception.UserEmailDuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuthLoginService oAuthLoginService;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByEmail_WhenEmailExists_ThenReturnUser() {
        //Given
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        UserEntityDto userEntityDto = UserEntityDto.create(
                userId, email, "testUser", "ACTIVE"
        );
        when(userRepository.getByEmail(email)).thenReturn(userEntityDto);

        //When
        User result = userService.getUserByEmail(email);

        //Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals("testUser", result.getNickname());
    }

    @Test
    void checkEmailUniqueness_WhenEmailNotExists_ThenReturnUnique() {
        //Given
        String email = "test@example.com";
        when(userRepository.findOptionalByEmail(email)).thenReturn(Optional.empty());

        //When
        UniqueMailResponse response = userService.checkEmailUniqueness(email);

        //Then
        assertTrue(response.isUnique());
        assertEquals("사용 가능한 이메일입니다.", response.message());
    }

    @Test
    void checkEmailUniqueness_WhenEmailWithdrawn_ThenReturnWithdrawn() {
        //Given
        String email = "test@example.com";
        UserEntityDto withdrawnUser = UserEntityDto.create(
                UUID.randomUUID(), email, "testUser", "WITHDRAWN"
        );
        when(userRepository.findOptionalByEmail(email)).thenReturn(Optional.of(withdrawnUser));

        //When
        UniqueMailResponse response = userService.checkEmailUniqueness(email);

        //Then
        assertFalse(response.isUnique());
        assertEquals("탈퇴한 사용자의 이메일입니다.", response.message());
    }

    @Test
    void checkEmailUniqueness_WhenEmailInactive_ThenReturnInactive() {
        //Given
        String email = "test@example.com";
        UserEntityDto inactiveUser = UserEntityDto.create(
                UUID.randomUUID(), email, "testUser", "INACTIVE"
        );
        when(userRepository.findOptionalByEmail(email)).thenReturn(Optional.of(inactiveUser));

        //When
        UniqueMailResponse response = userService.checkEmailUniqueness(email);

        //Then
        assertFalse(response.isUnique());
        assertEquals("휴면 유저의 이메일입니다.", response.message());
    }

    @Test
    void checkEmailUniqueness_WhenEmailActive_ThenReturnDuplicate() {
        //Given
        String email = "test@example.com";
        UserEntityDto activeUser = UserEntityDto.create(
                UUID.randomUUID(), email, "testUser", "ACTIVE"
        );
        when(userRepository.findOptionalByEmail(email)).thenReturn(Optional.of(activeUser));

        //When
        UniqueMailResponse response = userService.checkEmailUniqueness(email);

        //Then
        assertFalse(response.isUnique());
        assertEquals("사용 중인 이메일입니다.", response.message());
    }

    @Test
    void signUp_WhenEmailUnique_ThenCreateUser() {
        //Given
        SignupRequest request = new SignupRequest("test@example.com", "testUser", "id",
                OAuthProvider.KAKAO, "phone");
        UUID userId = UUID.randomUUID();

        when(userRepository.findOptionalByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.save(request.email(), request.nickname(), "ACTIVE"))
                .thenReturn(
                        UserEntityDto.create(userId, request.email(), request.nickname(), "ACTIVE"));
        when(oAuthLoginService.linkOAuthWithUser(any(), any(), any(), any())).thenReturn(
                any(JwtResponse.class));

        //When
        userService.signUp(request);

        //Then
        verify(userRepository).save(request.email(), request.nickname(), "ACTIVE");
    }

    @Test
    void signUp_WhenEmailDuplicate_ThenThrowException() {
        //Given
        SignupRequest request = new SignupRequest("test@example.com", "testUser", "id",
                OAuthProvider.KAKAO, "phone");
        UserEntityDto existingUser = UserEntityDto.create(
                UUID.randomUUID(), request.email(), "existingUser", "ACTIVE"
        );
        when(userRepository.findOptionalByEmail(request.email())).thenReturn(
                Optional.of(existingUser));

        //When & Then
        Exception exception = assertThrows(UserEmailDuplicateException.class, () ->
                userService.signUp(request)
        );
        assertTrue(exception.getMessage().contains("사용 중인 이메일입니다."));
    }
}

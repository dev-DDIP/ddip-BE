package com.knu.ddip.auth.business.validator;

import com.knu.ddip.auth.business.dto.TokenDTO;
import com.knu.ddip.auth.business.service.JwtFactory;
import com.knu.ddip.auth.business.service.TokenRepository;
import com.knu.ddip.auth.domain.Token;
import com.knu.ddip.auth.domain.TokenType;
import com.knu.ddip.auth.exception.TokenBadRequestException;
import com.knu.ddip.auth.exception.TokenConflictException;
import com.knu.ddip.auth.exception.TokenExpiredException;
import com.knu.ddip.auth.exception.TokenStolenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidatorTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtFactory jwtFactory;

    private JwtValidator JWTValidator;
    private UUID userId;
    private String deviceType;

    @BeforeEach
    void setUp() {
        JWTValidator = new JwtValidator(tokenRepository, jwtFactory);
        userId = UUID.randomUUID();
        deviceType = "web";
    }

    @Test
    public void validateAccessToken_whenValidAccessToken_noExceptionThrown() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        Token token = Token.of(TokenType.ACCESS, "value", userId.toString(), new Date(), future);

        //When, Then
        assertThatCode(() -> JWTValidator.validateAccessToken(token))
                .doesNotThrowAnyException();
    }

    @Test
    public void validateAccessToken_whenNotAccessToken_throwJWTBadRequestException() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        Token token = Token.of(TokenType.REFRESH, "value", userId.toString(), new Date(), future);

        //When, Then
        assertThatThrownBy(() -> JWTValidator.validateAccessToken(token))
                .isInstanceOf(TokenBadRequestException.class);
    }

    @Test
    public void validateAccessToken_whenExpiredToken_throwAccessTokenExpiredException() {
        //Given
        Date past = new Date(System.currentTimeMillis() - 1000 * 60);
        Token token = Token.of(TokenType.ACCESS, "value", userId.toString(), new Date(), past);

        //When, Then
        assertThatThrownBy(() -> JWTValidator.validateAccessToken(token))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    public void validateRefreshToken_whenValidRefreshToken_noExceptionThrown() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        String tokenValue = "refresh-token-value";
        Token token = Token.of(TokenType.REFRESH, tokenValue, userId.toString(), new Date(),
                future);
        TokenDTO tokenDTO = token.toTokenDTO();

        when(tokenRepository.findToken(userId, deviceType)).thenReturn(tokenDTO);
        when(jwtFactory.parseToken(tokenDTO.value())).thenReturn(Optional.of(token));
        when(tokenRepository.getLastRefreshTime(userId, deviceType)).thenReturn(Optional.of(0L));

        //When, Then
        assertThatCode(() -> JWTValidator.validateRefreshToken(token, userId, deviceType))
                .doesNotThrowAnyException();
    }

    @Test
    public void validateRefreshToken_whenNotRefreshToken_throwJWTBadRequestException() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        Token token = Token.of(TokenType.ACCESS, "value", userId.toString(), new Date(), future);

        //When, Then
        assertThatThrownBy(() -> JWTValidator.validateRefreshToken(token, userId, deviceType))
                .isInstanceOf(TokenBadRequestException.class);
    }

    @Test
    public void validateRefreshToken_givenNullStoredTokenValue_throwsTokenBadRequestException() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        Token nullValueToken = Token.of(TokenType.REFRESH, null, userId.toString(), new Date(),
                future);
        TokenDTO nullValueTokenDTO = nullValueToken.toTokenDTO();

        //When
        when(tokenRepository.findToken(userId, deviceType)).thenReturn(nullValueTokenDTO);

        //Then
        assertThatThrownBy(
                () -> JWTValidator.validateRefreshToken(nullValueToken, userId, deviceType))
                .isInstanceOf(TokenBadRequestException.class);
    }

    @Test
    public void validateRefreshToken_whenExpiredToken_throwAccessTokenExpiredException() {
        //Given
        Date past = new Date(System.currentTimeMillis() - 1000 * 60);
        Token token = Token.of(TokenType.REFRESH, "value", userId.toString(), new Date(), past);

        //When, Then
        assertThatThrownBy(() -> JWTValidator.validateRefreshToken(token, userId, deviceType))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    public void validateRefreshToken_whenTokenMismatch_throwRefreshTokenStolenException() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        Token storedToken = Token.of(TokenType.REFRESH, "stored-value", userId.toString(),
                new Date(), future);
        Token requestToken = Token.of(TokenType.REFRESH, "different-value", userId.toString(),
                new Date(),
                future);
        TokenDTO storedTokenDTO = requestToken.toTokenDTO();

        when(tokenRepository.findToken(userId, deviceType)).thenReturn(storedTokenDTO);
        when(jwtFactory.parseToken(storedTokenDTO.value())).thenReturn(Optional.of(storedToken));

        //When, Then
        assertThatThrownBy(
                () -> JWTValidator.validateRefreshToken(requestToken, userId, deviceType))
                .isInstanceOf(TokenStolenException.class);
        verify(tokenRepository).removeToken(userId, deviceType);
    }

    @Test
    public void validateRefreshToken_whenRecentRefresh_throwJWTConflictException() {
        //Given
        Date future = new Date(System.currentTimeMillis() + 1000 * 60);
        String tokenValue = "refresh-token-value";
        Token token = Token.of(TokenType.REFRESH, tokenValue, userId.toString(), new Date(),
                future);
        TokenDTO tokenDTO = token.toTokenDTO();

        long recentTime = System.currentTimeMillis() - 30 * 1000;

        when(tokenRepository.findToken(userId, deviceType)).thenReturn(tokenDTO);
        when(jwtFactory.parseToken(tokenDTO.value())).thenReturn(Optional.of(token));
        when(tokenRepository.getLastRefreshTime(userId, deviceType)).thenReturn(
                Optional.of(recentTime));

        //When, Then
        assertThatThrownBy(() -> JWTValidator.validateRefreshToken(token, userId, deviceType))
                .isInstanceOf(TokenConflictException.class);
    }
}

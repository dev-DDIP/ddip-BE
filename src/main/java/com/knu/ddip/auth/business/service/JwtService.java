package com.knu.ddip.auth.business.service;

import com.knu.ddip.auth.business.dto.JwtRefreshRequest;
import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.auth.business.dto.TokenDTO;
import com.knu.ddip.auth.business.validator.JwtValidator;
import com.knu.ddip.auth.domain.Token;
import com.knu.ddip.auth.exception.TokenBadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtFactory jwtFactory;
    private final TokenRepository tokenRepository;
    private final JwtValidator JWTValidator;

    public JwtResponse refreshAccessToken(JwtRefreshRequest request) {
        String refreshTokenValue = request.refreshToken();
        String deviceType = request.deviceType();

        Token refreshToken = jwtFactory.parseToken(refreshTokenValue)
                .orElseThrow(() -> new TokenBadRequestException("유효하지 않은 토큰입니다."));

        UUID userId = UUID.fromString(refreshToken.getSubject());

        JWTValidator.validateRefreshToken(refreshToken, userId, deviceType);

        Token newAccessToken = jwtFactory.createAccessToken(userId);
        Token newRefreshToken = jwtFactory.createRefreshToken(userId);

        TokenDTO newRefreshTokenDTO = newRefreshToken.toTokenDTO();

        tokenRepository.saveToken(userId, deviceType, newRefreshTokenDTO);
        tokenRepository.updateLastRefreshTime(userId, deviceType);

        return new JwtResponse(newAccessToken.getValue(), newRefreshToken.getValue());
    }

    public void logout(UUID userId, String deviceType) {
        tokenRepository.removeToken(userId, deviceType);
    }
}

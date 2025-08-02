package com.knu.ddip.auth.business.validator;

import com.knu.ddip.auth.business.dto.TokenDTO;
import com.knu.ddip.auth.business.service.JwtFactory;
import com.knu.ddip.auth.business.service.TokenRepository;
import com.knu.ddip.auth.domain.Token;
import com.knu.ddip.auth.exception.TokenBadRequestException;
import com.knu.ddip.auth.exception.TokenConflictException;
import com.knu.ddip.auth.exception.TokenExpiredException;
import com.knu.ddip.auth.exception.TokenStolenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtValidator {
    private static final long REFRESH_RATE_LIMIT_MILLISECONDS = 60 * 1000;

    private final TokenRepository tokenRepository;
    private final JwtFactory jwtFactory;

    public void validateRefreshToken(Token token, UUID userId, String deviceType) {
        if (!token.isRefreshToken()) {
            throw new TokenBadRequestException("리프레시 토큰이 아닙니다.");
        }

        if (token.isExpired()) {
            throw new TokenExpiredException("리프레시 토큰이 만료되었습니다.");
        }

        TokenDTO storedTokenDTO = tokenRepository.findToken(userId, deviceType);

        if (storedTokenDTO.value() == null) {
            throw new TokenBadRequestException("저장된 리프레시 토큰이 없습니다. 재로그인이 필요합니다.");
        }

        Token storedToken = jwtFactory.parseToken(storedTokenDTO.value())
                .orElseThrow(() -> new TokenBadRequestException("유효하지 않은 토큰입니다."));

        if (!storedToken.isSameValue(token.getValue())) {
            tokenRepository.removeToken(userId, deviceType);
            throw new TokenStolenException("리프레시 토큰이 일치하지 않습니다. 토큰 탈취 가능성.");
        }

        tokenRepository.getLastRefreshTime(userId, deviceType)
                .ifPresent(this::checkRefreshInterval);
    }

    private void checkRefreshInterval(long lastRefreshTime) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastRefreshTime) < REFRESH_RATE_LIMIT_MILLISECONDS) {
            throw new TokenConflictException("1분 이내에 이미 재발급 요청이 있었습니다.");
        }
    }

    public void validateAccessToken(Token token) {
        if (!token.isAccessToken()) {
            throw new TokenBadRequestException("액세스 토큰이 아닙니다.");
        }

        if (token.isExpired()) {
            throw new TokenExpiredException("액세스 토큰이 만료되었습니다.");
        }
    }
}

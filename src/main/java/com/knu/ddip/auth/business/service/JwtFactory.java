package com.knu.ddip.auth.business.service;

import com.knu.ddip.auth.domain.Token;
import com.knu.ddip.auth.domain.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtFactory {
    public static final long REFRESH_TOKEN_VALIDITY_MILLISECONDS = 14 * 24 * 60 * 60 * 1000;
    private static final long ACCESS_TOKEN_VALIDITY_MILLISECONDS = 30 * 60 * 1000;
    private final SecretKey secretKey;

    public JwtFactory(@Value("${SECRET_KEY}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Token createAccessToken(UUID userId) {
        return createToken(userId, TokenType.ACCESS, ACCESS_TOKEN_VALIDITY_MILLISECONDS);
    }

    public Token createRefreshToken(UUID userId) {
        return createToken(userId, TokenType.REFRESH, REFRESH_TOKEN_VALIDITY_MILLISECONDS);
    }

    private Token createToken(UUID userId, TokenType tokenType, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String tokenValue = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(validity)
                .claim("type", tokenType.name())
                .signWith(secretKey)
                .compact();

        return Token.of(tokenType, tokenValue, String.valueOf(userId), now, validity);
    }

    public Optional<Token> parseToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(tokenValue)
                    .getPayload();

            String subject = claims.getSubject();
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();
            String tokenTypeStr = claims.get("type", String.class);
            TokenType tokenType = TokenType.valueOf(tokenTypeStr);

            return Optional.of(Token.of(tokenType, tokenValue, subject, issuedAt, expiration));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

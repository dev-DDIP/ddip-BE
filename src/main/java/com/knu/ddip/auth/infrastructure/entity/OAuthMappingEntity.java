package com.knu.ddip.auth.infrastructure.entity;

import com.knu.ddip.auth.business.dto.OAuthMappingEntityDto;
import com.knu.ddip.auth.business.dto.OAuthTokenDto;
import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.auth.domain.OAuthToken;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "OAUTH_MAPPING")
public class OAuthMappingEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false)
    private String socialUserId;

    private String socialUserEmail;

    private String socialUserName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID userId;

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private LocalDateTime tokenIssuedAt;

    @Column(nullable = false)
    private boolean temporary;

    public static OAuthMappingEntity fromEntityDto(OAuthMappingEntityDto dto) {
        OAuthMappingEntity entity = OAuthMappingEntity.builder()
                .id(dto.getId())
                .socialUserId(dto.getSocialUserId())
                .socialUserEmail(dto.getSocialUserEmail())
                .socialUserName(dto.getSocialUserName())
                .provider(dto.getProvider())
                .userId(dto.getUserId())
                .temporary(dto.isTemporary())
                .build();

        OAuthToken oauthToken = dto.getOauthToken();
        if (oauthToken != null) {
            entity.accessToken = oauthToken.getAccessToken();
            entity.refreshToken = oauthToken.getRefreshToken();
            entity.expiresIn = oauthToken.getExpiresIn();
            entity.tokenIssuedAt = oauthToken.getIssuedAt();
        }

        return entity;
    }

    public OAuthMappingEntityDto toEntityDto() {
        OAuthToken oauthToken = OAuthToken.create(
                provider, accessToken, refreshToken, expiresIn
        );

        return OAuthMappingEntityDto.create(
                id,
                socialUserId,
                socialUserEmail,
                socialUserName,
                provider,
                userId,
                oauthToken,
                temporary
        );
    }

    public void updateFromDto(OAuthMappingEntityDto dto) {
        if (dto.getUserId() != null) {
            this.userId = dto.getUserId();
        }

        if (dto.getOauthToken() != null) {
            OAuthToken token = dto.getOauthToken();
            this.accessToken = token.getAccessToken();
            this.refreshToken = token.getRefreshToken();
            this.expiresIn = token.getExpiresIn();
            this.tokenIssuedAt = token.getIssuedAt();
        }

        if (dto.getSocialUserEmail() != null) {
            this.socialUserEmail = dto.getSocialUserEmail();
        }
        if (dto.getSocialUserName() != null) {
            this.socialUserName = dto.getSocialUserName();
        }
        this.temporary = dto.isTemporary();
    }

    public void updateFromTokenDto(OAuthTokenDto dto) {
        this.accessToken = dto.accessToken();
        this.refreshToken = dto.refreshToken();
        this.expiresIn = dto.expiresIn();
    }
}

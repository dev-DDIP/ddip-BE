package com.knu.ddip.auth.domain;

import com.knu.ddip.auth.business.dto.OAuthMappingEntityDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthMappingTest {

    @Test
    void fromDto_WithToken() {
        // Given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OAuthToken token = OAuthToken.builder()
                .provider(OAuthProvider.KAKAO)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .issuedAt(LocalDateTime.now())
                .build();

        OAuthMappingEntityDto dto = OAuthMappingEntityDto.create(
                id,
                "social-123",
                "user@example.com",
                "User Name",
                OAuthProvider.KAKAO,
                userId,
                token,
                false
        );

        // When
        OAuthMapping domain = OAuthMapping.fromDto(dto);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getSocialUserId()).isEqualTo("social-123");
        assertThat(domain.getSocialUserEmail()).isEqualTo("user@example.com");
        assertThat(domain.getSocialUserName()).isEqualTo("User Name");
        assertThat(domain.getProvider()).isEqualTo(OAuthProvider.KAKAO);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getOauthToken()).isNotNull();
        assertThat(domain.getOauthToken().getAccessToken()).isEqualTo("access-token");
        assertThat(domain.isTemporary()).isFalse();
    }

    @Test
    void createTemporary() {
        // Given
        String socialId = "social-123";
        String email = "user@example.com";
        String name = "User Name";
        OAuthProvider provider = OAuthProvider.KAKAO;

        OAuthToken token = OAuthToken.builder()
                .provider(provider)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .issuedAt(LocalDateTime.now())
                .build();

        // When
        OAuthMapping domain = OAuthMapping.createTemporary(
                socialId, email, name, provider, token
        );

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isNull();
        assertThat(domain.getSocialUserId()).isEqualTo(socialId);
        assertThat(domain.getSocialUserEmail()).isEqualTo(email);
        assertThat(domain.getSocialUserName()).isEqualTo(name);
        assertThat(domain.getProvider()).isEqualTo(provider);
        assertThat(domain.getUserId()).isNull();
        assertThat(domain.getOauthToken()).isEqualTo(token);
        assertThat(domain.isTemporary()).isTrue();
    }

    @Test
    void linkToUser_ShouldCreateNewInstanceWithUpdatedUserIdAndTemporaryFlag() {
        // Given
        UUID id = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        OAuthToken token = OAuthToken.builder()
                .provider(OAuthProvider.KAKAO)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .issuedAt(LocalDateTime.now())
                .build();

        OAuthMapping domain = OAuthMapping.builder()
                .id(id)
                .socialUserId("social-123")
                .socialUserEmail("user@example.com")
                .socialUserName("User Name")
                .provider(OAuthProvider.KAKAO)
                .oauthToken(token)
                .temporary(true)
                .build();

        // When
        OAuthMapping linkedDomain = domain.linkToUser(newUserId);

        // Then
        assertThat(linkedDomain).isNotNull();
        assertThat(linkedDomain).isNotSameAs(domain);
        assertThat(linkedDomain.getId()).isEqualTo(id);
        assertThat(linkedDomain.getUserId()).isEqualTo(newUserId);
        assertThat(linkedDomain.isTemporary()).isFalse();

        assertThat(domain.getUserId()).isNull();
        assertThat(domain.isTemporary()).isTrue();
    }

    @Test
    void toDto_ShouldMapCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime issuedAt = LocalDateTime.now();

        OAuthToken token = OAuthToken.builder()
                .provider(OAuthProvider.KAKAO)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .issuedAt(issuedAt)
                .build();

        OAuthMapping domain = OAuthMapping.builder()
                .id(id)
                .socialUserId("social-123")
                .socialUserEmail("user@example.com")
                .socialUserName("User Name")
                .provider(OAuthProvider.KAKAO)
                .userId(userId)
                .oauthToken(token)
                .temporary(false)
                .build();

        // When
        OAuthMappingEntityDto dto = domain.toDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getSocialUserId()).isEqualTo("social-123");
        assertThat(dto.getSocialUserEmail()).isEqualTo("user@example.com");
        assertThat(dto.getSocialUserName()).isEqualTo("User Name");
        assertThat(dto.getProvider()).isEqualTo(OAuthProvider.KAKAO);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getOauthToken()).isEqualTo(token);
        assertThat(dto.isTemporary()).isFalse();
    }
}

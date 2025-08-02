package com.knu.ddip.auth.business.dto;

import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.auth.domain.OAuthToken;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class OAuthMappingEntityDto {
    private final UUID id;
    private final String socialUserId;
    private final String socialUserEmail;
    private final String socialUserName;
    private final OAuthProvider provider;
    private final UUID userId;
    private final OAuthToken oauthToken;
    private final boolean temporary;

    public static OAuthMappingEntityDto create(UUID id, String providerId, String providerEmail,
                                               String providerName, OAuthProvider provider,
                                               UUID userId, OAuthToken oauthToken, boolean temporary) {
        return OAuthMappingEntityDto.builder()
                .id(id)
                .socialUserId(providerId)
                .socialUserEmail(providerEmail)
                .socialUserName(providerName)
                .provider(provider)
                .userId(userId)
                .oauthToken(oauthToken)
                .temporary(temporary)
                .build();
    }
}

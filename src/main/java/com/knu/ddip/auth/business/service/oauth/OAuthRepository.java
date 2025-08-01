package com.knu.ddip.auth.business.service.oauth;

import com.knu.ddip.auth.business.dto.OAuthMappingEntityDto;
import com.knu.ddip.auth.business.dto.OAuthTokenDto;
import com.knu.ddip.auth.domain.OAuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface OAuthRepository {

    OAuthMappingEntityDto save(OAuthMappingEntityDto entityDto);

    Optional<OAuthMappingEntityDto> findBySocialUserIdAndProvider(String socialUserId,
                                                                  OAuthProvider provider);

    Optional<OAuthMappingEntityDto> findByOauthMappingEntityIdAndProvider(UUID OauthMappingEntityId,
                                                                          OAuthProvider provider);

    void update(OAuthMappingEntityDto entityDto);

    void updateToken(UUID mappingId, OAuthTokenDto oauthTokenDto);
}

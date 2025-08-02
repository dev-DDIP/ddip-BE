package com.knu.ddip.auth.business.service;

import com.knu.ddip.auth.business.dto.TokenDTO;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository {
    void saveToken(UUID userId, String deviceType, TokenDTO tokenDTO);

    TokenDTO findToken(UUID userId, String deviceType);

    void removeToken(UUID userId, String deviceType);

    Optional<Long> getLastRefreshTime(UUID userId, String deviceType);

    void updateLastRefreshTime(UUID userId, String deviceType);
}

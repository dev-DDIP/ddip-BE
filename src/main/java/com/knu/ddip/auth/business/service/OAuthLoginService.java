package com.knu.ddip.auth.business.service;

import com.knu.ddip.auth.business.dto.*;
import com.knu.ddip.auth.business.service.oauth.OAuthRepository;
import com.knu.ddip.auth.business.service.oauth.OAuthService;
import com.knu.ddip.auth.domain.*;
import com.knu.ddip.auth.exception.OAuthBadRequestException;
import com.knu.ddip.auth.exception.OAuthNotFoundException;
import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.business.service.UserFactory;
import com.knu.ddip.user.business.service.UserRepository;
import com.knu.ddip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final Map<String, OAuthService> oAuthServices;
    private final OAuthRepository oAuthRepository;
    private final UserRepository userRepository;
    private final JwtFactory jwtFactory;
    private final TokenRepository tokenRepository;

    @Value("${OAUTH_APP_REDIRECT_URI}")
    private String appRedirectUri;

    public String getOAuthLoginUrl(String provider, String state) {
        OAuthContext context = parseOAuthContext(provider, state);

        OAuthService oAuthService = getOAuthService(context.oAuthProvider());

        if (!oAuthService.isBackendRedirect()) {
            throw new OAuthBadRequestException("이 제공자는 백엔드 리다이렉트를 지원하지 않습니다.");
        }

        return oAuthService.getRedirectUrl(context.deviceType().name());
    }

    @Transactional
    public URI handleOAuthCallback(String provider, String code, String state) {
        OAuthContext context = parseOAuthContext(provider, state);

        OAuthLoginResponse loginResponse = processOAuthLogin(context.oAuthProvider(), code,
                context.deviceType());

        StringBuilder redirectBuilder = new StringBuilder(appRedirectUri);

        String urlFragmentDelimiter = "#";

        redirectBuilder.append(urlFragmentDelimiter)
                .append("needRegister=").append(loginResponse.needRegister());

        if (loginResponse.needRegister()) {
            redirectBuilder.append("&provider=")
                    .append(context.oAuthProvider().name().toLowerCase())
                    .append("&deviceType=")
                    .append(context.deviceType().name().toLowerCase())
                    .append("&oAuthMappingEntityId=")
                    .append(loginResponse.OAuthMappingEntityId());
        } else {
            redirectBuilder.append("&accessToken=").append(loginResponse.accessToken())
                    .append("&refreshToken=").append(loginResponse.refreshToken());
        }

        return URI.create(redirectBuilder.toString());
    }

    private OAuthContext parseOAuthContext(String provider, String state) {
        OAuthProvider oAuthProvider = OAuthProvider.fromString(provider);
        DeviceType deviceType = DeviceType.fromString(state);
        return OAuthContext.of(oAuthProvider, deviceType);
    }

    @Transactional
    public OAuthLoginResponse processOAuthLogin(OAuthProvider provider, String code,
                                                DeviceType deviceType) {
        OAuthService oAuthService = getOAuthService(provider);

        OAuthUserInfo userInfo = oAuthService.getUserInfo(code);

        OAuthToken oauthToken = userInfo.getOAuthToken();

        Optional<OAuthMappingEntityDto> existingMappingDto =
                oAuthRepository.findBySocialUserIdAndProvider(userInfo.getSocialUserId(), provider);

        if (existingMappingDto.isPresent()) {
            OAuthMappingEntityDto mappingDto = existingMappingDto.get();

            OAuthTokenDto oauthTokenDto = OAuthTokenDto.from(oauthToken);
            oAuthRepository.updateToken(mappingDto.getId(), oauthTokenDto);

            if (mappingDto.getUserId() != null && !mappingDto.isTemporary()) {
                JwtResponse jwtResponse = generateTokensForUser(mappingDto.getUserId(),
                        deviceType);
                return OAuthLoginResponse.toJwt(jwtResponse.accessToken(),
                        jwtResponse.refreshToken());
            } else {
                return OAuthLoginResponse.toSignUp(mappingDto.getId());
            }
        }

        OAuthMapping tempMapping = OAuthMapping.createTemporary(
                userInfo.getSocialUserId(),
                userInfo.getEmail(),
                userInfo.getName(),
                provider,
                oauthToken
        );

        OAuthMappingEntityDto newMapping = tempMapping.toDto();
        OAuthMappingEntityDto savedMapping = oAuthRepository.save(newMapping);

        return OAuthLoginResponse.toSignUp(savedMapping.getId());
    }

    @Transactional
    public JwtResponse linkOAuthWithUser(User user, String oauthMappingEntityId,
                                         OAuthProvider provider, DeviceType deviceType) {
        Optional<OAuthMappingEntityDto> oAuthMappingEntityDtoOpt =
                oAuthRepository.findByOauthMappingEntityIdAndProvider(
                        UUID.fromString(oauthMappingEntityId), provider);

        if (oAuthMappingEntityDtoOpt.isEmpty()) {
            throw new OAuthNotFoundException("OAuth 정보를 찾을 수 없습니다.");
        }

        OAuthMappingEntityDto mappingDto = oAuthMappingEntityDtoOpt.get();

        OAuthMapping oAuthMappingDomain = OAuthMapping.fromDto(mappingDto);

        OAuthMapping updatedDomain = oAuthMappingDomain.linkToUser(user.getId());

        OAuthMappingEntityDto updatedDto = updatedDomain.toDto();
        oAuthRepository.update(updatedDto);

        return generateTokensForUser(user.getId(), deviceType);
    }

    private JwtResponse generateTokensForUser(UUID userId, DeviceType deviceType) {
        UserEntityDto userEntityDto = userRepository.getById(userId);
        UserFactory.create(userEntityDto.getId(), userEntityDto.getEmail(),
                userEntityDto.getNickname(), userEntityDto.getStatus());

        Token accessToken = jwtFactory.createAccessToken(userId);
        Token refreshToken = jwtFactory.createRefreshToken(userId);

        TokenDTO refreshTokenDTO = refreshToken.toTokenDTO();

        tokenRepository.saveToken(userId, deviceType.name(), refreshTokenDTO);

        return new JwtResponse(accessToken.getValue(), refreshToken.getValue());
    }

    private OAuthService getOAuthService(OAuthProvider provider) {
        String serviceBeanName = provider.name().toLowerCase() + "OAuthService";
        OAuthService service = oAuthServices.get(serviceBeanName);

        if (service == null) {
            throw new OAuthBadRequestException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }

        return service;
    }
}

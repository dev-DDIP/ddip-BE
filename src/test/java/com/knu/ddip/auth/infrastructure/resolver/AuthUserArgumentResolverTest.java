package com.knu.ddip.auth.infrastructure.resolver;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.auth.domain.Token;
import com.knu.ddip.auth.domain.TokenType;
import com.knu.ddip.auth.exception.TokenBadRequestException;
import com.knu.ddip.auth.presentation.annotation.Login;
import com.knu.ddip.auth.presentation.resolver.AuthUserArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    private static final String AUTH_TOKEN_ATTRIBUTE = "AUTH_TOKEN";

    @InjectMocks
    private AuthUserArgumentResolver resolver;

    @Mock
    private MethodParameter parameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private HttpServletRequest httpRequest;

    @Test
    public void supportsParameter_whenHasLoginAnnotationAndAuthUserType_returnsTrue() {
        // Given
        when(parameter.hasMethodAnnotation(Login.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) AuthUser.class);

        // When
        boolean result = resolver.supportsParameter(parameter);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void supportsParameter_whenNoLoginAnnotation_returnsFalse() {
        // Given
        when(parameter.hasMethodAnnotation(Login.class)).thenReturn(false);
        when(parameter.getParameterType()).thenReturn((Class) AuthUser.class);

        // When
        boolean result = resolver.supportsParameter(parameter);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void supportsParameter_whenNotAuthUserType_returnsFalse() {
        // Given
        when(parameter.hasMethodAnnotation(Login.class)).thenReturn(true);
        when(parameter.getParameterType()).thenReturn((Class) String.class);

        // When
        boolean result = resolver.supportsParameter(parameter);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void resolveArgument_whenTokenExists_returnsAuthUser() {
        // Given
        UUID userId = UUID.randomUUID();
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
        Token token = Token.of(TokenType.ACCESS, "token-value", String.valueOf(userId), new Date(),
                expiryDate);

        when(webRequest.getNativeRequest()).thenReturn(httpRequest);
        when(httpRequest.getAttribute(AUTH_TOKEN_ATTRIBUTE)).thenReturn(token);

        // When
        AuthUser authUser = (AuthUser) resolver.resolveArgument(parameter, mavContainer, webRequest,
                binderFactory);

        // Then
        assertThat(authUser).isNotNull();
        assertThat(authUser.getId()).isEqualTo(userId);
    }

    @Test
    public void resolveArgument_whenTokenNotExists_throwsJWTBadRequestException() {
        // Given
        when(webRequest.getNativeRequest()).thenReturn(httpRequest);
        when(httpRequest.getAttribute(AUTH_TOKEN_ATTRIBUTE)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() ->
                resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                .isInstanceOf(TokenBadRequestException.class);
    }
}

package com.knu.ddip.auth.presentation.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/auth/oauth")
@Tag(name = "OAuth", description = "OAuth 관련 API")
public interface OAuthApi {

    @GetMapping("/{provider}/login")
    @Operation(summary = "OAuth 로그인 페이지 이동",
            description = "provider별 로그인 페이지로 이동한다. state는 deviceType(TABLET/PHONE)")
    RedirectView redirectToOAuthLoginPage(
            @PathVariable("provider") String provider,
            @RequestParam(value = "state", required = false) String state);

    @GetMapping("/{provider}/callback")
    @Operation(summary = "OAuth용 콜백 [직접 사용 금지]",
            description = "provider 측에서 콜백 리다이렉트로 사용할 엔드포인트. 직접 사용 금지.")
    ResponseEntity<Void> handleOAuthCallback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            @RequestParam(value = "state") String state);
}

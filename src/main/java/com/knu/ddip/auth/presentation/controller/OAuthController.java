package com.knu.ddip.auth.presentation.controller;

import com.knu.ddip.auth.business.service.OAuthLoginService;
import com.knu.ddip.auth.presentation.api.OAuthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class OAuthController implements OAuthApi {

    private final OAuthLoginService oAuthLoginService;

    @Override
    public RedirectView redirectToOAuthLoginPage(
            @PathVariable("provider") String provider,
            @RequestParam(value = "state", required = false) String state) {

        String redirectUrl = oAuthLoginService.getOAuthLoginUrl(provider, state);

        return new RedirectView(redirectUrl);
    }

    @Override
    public ResponseEntity<Void> handleOAuthCallback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            @RequestParam(value = "state") String state) {

        URI redirectInfo = oAuthLoginService.handleOAuthCallback(provider, code, state);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirectInfo);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

package com.knu.ddip.user.presentation.controller;

import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.user.business.dto.DummyRequest;
import com.knu.ddip.user.business.dto.SignupRequest;
import com.knu.ddip.user.business.dto.UniqueMailResponse;
import com.knu.ddip.user.business.service.UserService;
import com.knu.ddip.user.presentation.api.UserApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<JwtResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        JwtResponse jwtResponse = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jwtResponse);
    }

    @Override
    public ResponseEntity<UniqueMailResponse> checkEmailUniqueness(
            @RequestParam("v") String email) {
        UniqueMailResponse result = userService.checkEmailUniqueness(email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @Override
    public ResponseEntity<JwtResponse> dummyLogin(DummyRequest dummyRequest) {
        JwtResponse jwtResponse = userService.dummyLogin(dummyRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    //TODO: 로그아웃
    //TODO: 회원 탈퇴
    //TODO: user 정보 수정
    //TODO: 유저 정보 등록 여부 확인
}

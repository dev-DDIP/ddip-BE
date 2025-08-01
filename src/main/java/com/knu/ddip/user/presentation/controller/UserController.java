package com.knu.ddip.user.presentation.controller;

import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.user.business.dto.SignupRequest;
import com.knu.ddip.user.business.dto.UniqueMailResponse;
import com.knu.ddip.user.business.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입",
            description = "Oauth 로직 통과 후 매칭되는 유저가 없을 시 회원가입을 진행한다.")
    public ResponseEntity<JwtResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        JwtResponse jwtResponse = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jwtResponse);
    }

    @GetMapping("/my/mail")
    @Operation(summary = "메일 중복조회",
            description = "메일이 사용 가능한지 조회한다. 휴면유저/탈퇴한 유저의 메일도 사용 불가.")
    public ResponseEntity<UniqueMailResponse> checkEmailUniqueness(
            @RequestParam("v") String email) {
        UniqueMailResponse result = userService.checkEmailUniqueness(email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    //TODO: 로그아웃
    //TODO: 회원 탈퇴
    //TODO: user 정보 수정
    //TODO: 유저 정보 등록 여부 확인
}

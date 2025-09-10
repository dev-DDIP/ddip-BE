package com.knu.ddip.user.presentation.api;

import com.knu.ddip.auth.business.dto.JwtResponse;
import com.knu.ddip.user.business.dto.DummyRequest;
import com.knu.ddip.user.business.dto.SignupRequest;
import com.knu.ddip.user.business.dto.UniqueMailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저", description = "유저 관련 API")
@RequestMapping("/api")
public interface UserApi {

    @PostMapping("/signup")
    @Operation(summary = "회원 가입",
            description = "Oauth 로직 통과 후 매칭되는 유저가 없을 시 회원가입을 진행한다.")
    ResponseEntity<JwtResponse> signup(
            @Valid @RequestBody SignupRequest request);

    @GetMapping("/my/mail")
    @Operation(summary = "메일 중복조회",
            description = "메일이 사용 가능한지 조회한다. 휴면유저/탈퇴한 유저의 메일도 사용 불가.")
    ResponseEntity<UniqueMailResponse> checkEmailUniqueness(
            @RequestParam("v") String email);

    @PostMapping("/dummy")
    @Operation(summary = "[테스트용] 임시 로그인용 JWT 생성", description = "테스트를 위해 OAuth를 거치지 않고도 서버에서 사용 가능한 인증용 JWT를 생성한다. 주의: 만약 이미 존재하는 유저의 경우 닉네임이 입력한 email에 기반한 유저의 인증용 jwt가 생성되고, 닉네임은 입력한 닉네임이 아닌 기존의 닉네임으로 유지된다.")
    ResponseEntity<JwtResponse> dummyLogin(
            @RequestBody DummyRequest dummyRequest);
}

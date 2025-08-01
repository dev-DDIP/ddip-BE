package com.knu.ddip.user.presentation.api;

import com.knu.ddip.auth.business.dto.JwtResponse;
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
}

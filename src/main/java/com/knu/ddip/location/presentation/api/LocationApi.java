package com.knu.ddip.location.presentation.api;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.auth.presentation.annotation.Login;
import com.knu.ddip.auth.presentation.annotation.RequireAuth;
import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "위치", description = "위치 관련 API")
@RequestMapping("/api/locations")
public interface LocationApi {

    @PutMapping
    @RequireAuth
    @Operation(summary = "위치 갱신",
            description = "위도와 경도로 현재 내 위치를 갱신한다.")
    ResponseEntity<Void> updateMyLocation(
            @Parameter(hidden = true) @Login AuthUser user,
            @RequestBody UpdateMyLocationRequest request
    );
}

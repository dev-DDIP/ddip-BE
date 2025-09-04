package com.knu.ddip.ddipevent.presentation.api;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.auth.presentation.annotation.Login;
import com.knu.ddip.auth.presentation.annotation.RequireAuth;
import com.knu.ddip.ddipevent.application.dto.CreateDdipRequestDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventDetailDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventSummaryDto;
import com.knu.ddip.ddipevent.application.dto.FeedRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "DDIP Event")
@RequestMapping("/api/ddips")
public interface DdipApi {

    @Operation(summary = "DDIP Event 생성", description = "DDIP Event를 생성한다.")
    @PostMapping
    @RequireAuth
    ResponseEntity<DdipEventDetailDto> createDdipEvent(
            @RequestBody CreateDdipRequestDto createDdipRequestDto,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "DDIP Event들 범위 기반 조회", description = "범위에 기반한 DDIP Event들을 불러온다.")
    @GetMapping
    ResponseEntity<List<DdipEventSummaryDto>> getDdipEventFeed(
            @ModelAttribute FeedRequestDto feedRequestDto
    );

    @Operation(summary = "DDIP Event 상세 조회", description = "ddipId에 해당하는 DDIP Event를 상세 조회한다.")
    @GetMapping("/{ddipId}")
    ResponseEntity<DdipEventDetailDto> getDdipEventDetail(
            @PathVariable UUID ddipId
    );
}

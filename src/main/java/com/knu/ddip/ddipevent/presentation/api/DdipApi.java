package com.knu.ddip.ddipevent.presentation.api;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.auth.presentation.annotation.Login;
import com.knu.ddip.auth.presentation.annotation.RequireAuth;
import com.knu.ddip.common.dto.StringTypeResponse;
import com.knu.ddip.ddipevent.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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

    @Operation(summary = "DDIP Event 상세 조회", description = "eventId에 해당하는 DDIP Event를 상세 조회한다.")
    @GetMapping("/{eventId}")
    ResponseEntity<DdipEventDetailDto> getDdipEventDetail(
            @PathVariable UUID eventId
    );

    @Operation(summary = "DDIP event 지원하기", description = "OPEN 상태의 DDIP event에 지원한다.")
    @PostMapping("/{eventId}/apply")
    @RequireAuth
    ResponseEntity<StringTypeResponse> applyDdipEvent(
            @PathVariable UUID eventId,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "DDIP event 지원자 중 수행자 선택하기", description = "본인이 생성한 DDIP event의 지원자 목록에서 특정 사용자를 선택한다.")
    @PostMapping("/{eventId}/select")
    @RequireAuth
    ResponseEntity<StringTypeResponse> selectApplicantForDdipEvent(
            @PathVariable UUID eventId,
            @RequestBody SelectApplicantRequest selectApplicantRequest,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "수행 증거 사진 제출하기", description = "수행자가 미션 수행 결과 사진을 서버에 업로드한다.")
    @PostMapping(value = "/{eventId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireAuth
    ResponseEntity<DdipEventDetailDto> uploadPhotoForDdipEvent(
            @PathVariable UUID eventId,
            @ModelAttribute PhotoUploadRequest photoUploadRequest,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "제출된 사진 피드백 업데이트하기", description = "요청자, 수행자 모두 제출된 사진에 대한 피드백을 업데이트한다.")
    @PatchMapping("/{eventId}/photos/{photoId}")
    @RequireAuth
    ResponseEntity<DdipEventDetailDto> updatePhotoFeedback(
            @PathVariable(value = "eventId") UUID eventId,
            @PathVariable(value = "photoId") UUID photoId,
            @RequestBody PhotoFeedbackRequest photoFeedbackRequest,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "미션 최종 완료 처리하기", description = "수행자가 미션 수행을 완료 처리 한다.")
    @PostMapping("/{eventId}/complete")
    @RequireAuth
    ResponseEntity<DdipEventDetailDto> completeDdipEventMission(
            @PathVariable UUID eventId,
            @Parameter(hidden = true) @Login AuthUser authUser
    );

    @Operation(summary = "미션 중단하기", description = "요청자/수행자가 미션 수행을 중단/포기 처리 한다.")
    @PostMapping("/{eventId}/cancel")
    @RequireAuth
    ResponseEntity<DdipEventDetailDto> cancelDdipEventMission(
            @PathVariable UUID eventId,
            @Parameter(hidden = true) @Login AuthUser authUser
    );
}

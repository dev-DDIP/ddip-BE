package com.knu.ddip.ddipevent.presentation.controller;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.common.dto.StringTypeResponse;
import com.knu.ddip.ddipevent.application.dto.*;
import com.knu.ddip.ddipevent.application.service.DdipService;
import com.knu.ddip.ddipevent.presentation.api.DdipApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DdipController implements DdipApi {

    private final DdipService ddipService;

    @Override
    public ResponseEntity<DdipEventDetailDto> createDdipEvent(CreateDdipRequestDto createDdipRequestDto, AuthUser authUser) {
        DdipEventDetailDto createdDdip = ddipService.createDdipEvent(createDdipRequestDto, authUser.getId());
        return ResponseEntity.ok(createdDdip);
    }

    @Override
    public ResponseEntity<List<DdipEventSummaryDto>> getDdipEventFeed(FeedRequestDto feedRequestDto) {
        List<DdipEventSummaryDto> feed = ddipService.getDdipEventFeed(feedRequestDto);
        return ResponseEntity.ok(feed);
    }

    @Override
    public ResponseEntity<DdipEventDetailDto> getDdipEventDetail(UUID eventId) {
        DdipEventDetailDto ddipDetail = ddipService.getDdipEventDetail(eventId);
        return ResponseEntity.ok(ddipDetail);
    }

    @Override
    public ResponseEntity<StringTypeResponse> applyDdipEvent(UUID eventId, AuthUser authUser) {
        ddipService.applyDdipEvent(eventId, authUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new StringTypeResponse("정상적으로 지원되었습니다."));
    }

    @Override
    public ResponseEntity<StringTypeResponse> selectApplicantForDdipEvent(UUID eventId, SelectApplicantRequest selectApplicantRequest, AuthUser authUser) {
        ddipService.selectApplicantForDdipEvent(eventId, selectApplicantRequest, authUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new StringTypeResponse("정상적으로 수행자를 선택하였습니다."));
    }

    @Override
    public ResponseEntity<DdipEventDetailDto> uploadPhotoForDdipEvent(UUID eventId, PhotoUploadRequest photoUploadRequest, AuthUser authUser) {
        DdipEventDetailDto ddipEventDetailDto = ddipService.uploadPhotoForDdipEvent(eventId, photoUploadRequest, authUser.getId());
        return ResponseEntity.ok(ddipEventDetailDto);
    }

    @Override
    public ResponseEntity<DdipEventDetailDto> updatePhotoFeedback(UUID eventId, UUID photoId, PhotoFeedbackRequest photoFeedbackRequest, AuthUser authUser) {
        DdipEventDetailDto ddipEventDetailDto = ddipService.updatePhotoFeedback(eventId, photoId, photoFeedbackRequest, authUser.getId());
        return ResponseEntity.ok(ddipEventDetailDto);
    }

    @Override
    public ResponseEntity<DdipEventDetailDto> completeDdipEventMission(UUID eventId, AuthUser authUser) {
        DdipEventDetailDto ddipEventDetailDto = ddipService.completeDdipEventMission(eventId, authUser.getId());
        return ResponseEntity.ok(ddipEventDetailDto);
    }

    @Override
    public ResponseEntity<DdipEventDetailDto> cancelDdipEventMission(UUID eventId, AuthUser authUser) {
        DdipEventDetailDto ddipEventDetailDto = ddipService.cancelDdipEventMission(eventId, authUser.getId());
        return ResponseEntity.ok(ddipEventDetailDto);
    }
}

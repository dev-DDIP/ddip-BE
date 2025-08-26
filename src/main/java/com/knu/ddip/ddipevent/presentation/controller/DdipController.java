package com.knu.ddip.ddipevent.presentation.controller;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.ddipevent.application.dto.CreateDdipRequestDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventDetailDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventSummaryDto;
import com.knu.ddip.ddipevent.application.dto.FeedRequestDto;
import com.knu.ddip.ddipevent.application.service.DdipService;
import com.knu.ddip.ddipevent.presentation.api.DdipApi;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<DdipEventDetailDto> getDdipEventDetail(UUID ddipId) {
        DdipEventDetailDto ddipDetail = ddipService.getDdipEventDetail(ddipId);
        return ResponseEntity.ok(ddipDetail);
    }
}

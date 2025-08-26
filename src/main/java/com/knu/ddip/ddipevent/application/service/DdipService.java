package com.knu.ddip.ddipevent.application.service;

import com.knu.ddip.ddipevent.application.dto.*;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.exception.DdipNotFoundException;
import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.business.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DdipService {

    private final DdipEventRepository ddipEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public DdipEventDetailDto createDdipEvent(CreateDdipRequestDto dto, UUID requesterId) {
        UserEntityDto requester = userRepository.getById(requesterId);
        DdipEvent newDdipEvent = DdipEvent.create(dto.title(), dto.content(),
                dto.reward(), dto.latitude(), dto.longitude(), dto.difficulty(), requester.getId());

        DdipEvent savedDdip = ddipEventRepository.save(newDdipEvent);
        return convertToDetailDto(savedDdip);
    }

    public List<DdipEventSummaryDto> getDdipEventFeed(FeedRequestDto dto) {
        List<DdipEvent> events = ddipEventRepository.findWithinBounds(
                dto.sw_lat(), dto.sw_lon(), dto.ne_lat(), dto.ne_lon(), dto.sort(), dto.user_lat(), dto.user_lon());

        return events.stream()
                .map(this::convertToSummaryDto)
                .toList();
    }

    public DdipEventDetailDto getDdipEventDetail(UUID ddipId) {
        DdipEvent event = ddipEventRepository.findById(ddipId)
                .orElseThrow(() -> new DdipNotFoundException("Ddip event를 찾을 수 없습니다."));
        return convertToDetailDto(event);
    }

    private DdipEventSummaryDto convertToSummaryDto(DdipEvent event) {
        // TODO: distance(요청자와 사용자 사이의 거리) 계산 로직 추가
        return new DdipEventSummaryDto(
                event.getId().toString(),
                event.getTitle(),
                event.getReward(),
                event.getLatitude(),
                event.getLongitude(),
                event.getStatus(),
                event.getRequesterId().toString(),
                event.getCreatedAt().toString(),
                event.getApplicants().size(),
                event.getContent(),
                0.0, // 임시값으로 0.0
                event.getDifficulty()
        );
    }

    private DdipEventDetailDto convertToDetailDto(DdipEvent event) {
        // TODO: 유저 관련 로직 추가 필요
        return new DdipEventDetailDto(
                event.getId().toString(),
                event.getTitle(),
                event.getContent(),
                event.getReward(),
                event.getLatitude(),
                event.getLongitude(),
                event.getStatus(),
                event.getCreatedAt().toString(),
                null, // TODO: applicants
                null, // TODO: selectedResponder
                event.getPhotos().stream()
                        .map(PhotoDto::fromEntity)
                        .toList(),
                event.getInteractions().stream()
                        .map(InteractionDto::fromEntity)
                        .toList()
        );
    }
}

package com.knu.ddip.ddipevent.application.service;

import com.knu.ddip.ddipevent.application.dto.*;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.exception.DdipNotFoundException;
import com.knu.ddip.ddipevent.application.util.DistanceConverter;
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
    private final DistanceConverter distanceConverter;

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
                .map(event -> convertToSummaryDto(event, dto.user_lat(), dto.user_lon()))
                .toList();
    }

    public DdipEventDetailDto getDdipEventDetail(UUID ddipId) {
        DdipEvent event = ddipEventRepository.findById(ddipId)
                .orElseThrow(() -> new DdipNotFoundException("Ddip event를 찾을 수 없습니다."));
        return convertToDetailDto(event);
    }

    private DdipEventSummaryDto convertToSummaryDto(DdipEvent event, Double userLat, Double userLon) {
        double dist = distanceConverter.haversineMeters(event.getLatitude(), event.getLongitude(), userLat, userLon);
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
                dist,
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

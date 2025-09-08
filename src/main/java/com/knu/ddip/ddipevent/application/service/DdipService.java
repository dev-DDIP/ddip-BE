package com.knu.ddip.ddipevent.application.service;

import com.knu.ddip.common.file.FileStorageService;
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
    private final FileStorageService fileStorageService;
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

    public DdipEventDetailDto getDdipEventDetail(UUID eventId) {
        DdipEvent event = getDdipEvent(eventId);
        return convertToDetailDto(event);
    }

    @Transactional
    public void applyDdipEvent(UUID eventId, UUID responderId) {
        UserEntityDto responder = userRepository.getById(responderId);
        DdipEvent event = getDdipEvent(eventId);
        DdipEvent updatedEvent = event.apply(responder.getId());
        ddipEventRepository.save(updatedEvent);
    }

    @Transactional
    public void selectApplicantForDdipEvent(UUID eventId, SelectApplicantRequest selectApplicantRequest, UUID requesterId) {
        UserEntityDto requester = userRepository.getById(requesterId);
        UserEntityDto responder = userRepository.getById(selectApplicantRequest.applicantId());
        DdipEvent event = getDdipEvent(eventId);
        DdipEvent updatedEvent = event.selectResponder(requester.getId(), responder.getId());
        ddipEventRepository.save(updatedEvent);
    }

    @Transactional
    public DdipEventDetailDto uploadPhotoForDdipEvent(UUID eventId, PhotoUploadRequest photoUploadRequest, UUID responderId) {
        UserEntityDto responder = userRepository.getById(responderId);
        DdipEvent event = getDdipEvent(eventId);

        String photoUrl = fileStorageService.uploadFile(photoUploadRequest.photo(), "photos");

        DdipEvent updatedEvent = event.uploadPhoto(responder.getId(), photoUrl, photoUploadRequest.latitude(), photoUploadRequest.longitude(), photoUploadRequest.responderComment());
        return convertToDetailDto(ddipEventRepository.save(updatedEvent));
    }

    @Transactional
    public DdipEventDetailDto updatePhotoFeedback(UUID eventId, UUID photoId, PhotoFeedbackRequest photoFeedbackRequest, UUID requesterOrResponderId) {
        UserEntityDto requesterOrResponder = userRepository.getById(requesterOrResponderId);
        DdipEvent event = getDdipEvent(eventId);
        DdipEvent updatedEvent = event.updatePhotoFeedback(requesterOrResponder.getId(), photoId, photoFeedbackRequest.status(), photoFeedbackRequest.feedback());
        return convertToDetailDto(ddipEventRepository.save(updatedEvent));
    }

    @Transactional
    public DdipEventDetailDto completeDdipEventMission(UUID eventId, UUID requesterId) {
        UserEntityDto requester = userRepository.getById(requesterId);
        DdipEvent event = getDdipEvent(eventId);
        DdipEvent updatedEvent = event.complete(requester.getId());
        return convertToDetailDto(ddipEventRepository.save(updatedEvent));
    }

    @Transactional
    public DdipEventDetailDto cancelDdipEventMission(UUID eventId, UUID requesterOrResponderId) {
        UserEntityDto requesterOrResponder = userRepository.getById(requesterOrResponderId);
        DdipEvent event = getDdipEvent(eventId);
        DdipEvent updatedEvent = event.cancel(requesterOrResponder.getId());
        return convertToDetailDto(ddipEventRepository.save(updatedEvent));
    }

    private DdipEvent getDdipEvent(UUID eventId) {
        return ddipEventRepository.findById(eventId)
                .orElseThrow(() -> new DdipNotFoundException("Ddip event를 찾을 수 없습니다."));
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
                event.getApplicants().stream().map(UUID::toString).map(UserSummaryDto::fromUserId).toList(), // TODO: 세부 구현
                event.getSelectedResponderId() != null
                        ? UserSummaryDto.fromUserId(event.getSelectedResponderId().toString()) : null, // TODO: 세부 구현
                event.getPhotos().stream()
                        .map(PhotoDto::fromEntity)
                        .toList(),
                event.getInteractions().stream()
                        .map(InteractionDto::fromEntity)
                        .toList()
        );
    }
}

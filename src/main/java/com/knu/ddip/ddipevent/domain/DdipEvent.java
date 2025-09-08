package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
import com.knu.ddip.ddipevent.exception.DdipNotFoundException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
public class DdipEvent {
    private final UUID id;
    private final UUID requesterId;
    private final Double latitude;
    private final Double longitude;
    private final Instant createdAt;
    private String title;
    private String content;
    private Integer reward;
    private DdipStatus status;
    private UUID selectedResponderId;
    private List<UUID> applicants;
    private List<Photo> photos;
    private List<Interaction> interactions;
    private Integer difficulty;

    public static DdipEvent create(String title, String content, Integer reward, Double latitude, Double longitude, Integer difficulty, UUID requesterId) {
        return DdipEvent.builder()
                .title(title)
                .content(content)
                .reward(reward)
                .latitude(latitude)
                .longitude(longitude)
                .difficulty(difficulty)
                .requesterId(requesterId)
                .createdAt(Instant.now())
                .status(DdipStatus.OPEN)
                .build();
    }

    public DdipEvent apply(UUID applicantId) {
        if (this.status != DdipStatus.OPEN) {
            throw new DdipBadRequestException("이미 마감된 띱입니다.");
        }
        if (Objects.equals(this.requesterId, applicantId)) {
            throw new DdipBadRequestException("자신의 띱에는 지원할 수 없습니다.");
        }
        if (!this.applicants.contains(applicantId)) {
            this.applicants.add(applicantId);
            this.interactions.add(Interaction.builder()
                    .actorId(applicantId)
                    .actorRole(ActorRole.RESPONDER)
                    .actionType(ActionType.APPLY)
                    .timestamp(Instant.now())
                    .build());
            return this;
        } else throw new DdipBadRequestException("이미 지원한 띱입니다.");
    }

    public DdipEvent selectResponder(UUID requesterId, UUID responderId) {
        if (!Objects.equals(this.requesterId, requesterId)) {
            throw new DdipForbiddenException("띱을 등록한 사용자만 수행자를 선택할 수 있습니다.");
        }
        if (this.status != DdipStatus.OPEN) {
            throw new DdipBadRequestException("이미 진행중이거나 마감된 띱입니다.");
        }
        if (!this.applicants.contains(responderId)) {
            throw new DdipBadRequestException("지원자 목록에 없는 사용자입니다.");
        }
        this.selectedResponderId = responderId;
        this.status = DdipStatus.IN_PROGRESS;
        this.interactions.add(Interaction.builder()
                .actorId(requesterId)
                .actorRole(ActorRole.REQUESTER)
                .actionType(ActionType.SELECT_RESPONDER)
                .timestamp(Instant.now())
                .build());
        return this;
    }

    public DdipEvent uploadPhoto(UUID responderId, String photoUrl, Double latitude, Double longitude, String responderComment) {
        if (!this.selectedResponderId.equals(responderId)) {
            throw new DdipForbiddenException("띱의 선택된 수행자만 사진을 업로드할 수 있습니다.");
        }
        if (this.status != DdipStatus.IN_PROGRESS) {
            throw new DdipBadRequestException("진행중인 띱에만 사진을 업로드할 수 있습니다.");
        }
        if (photoUrl == null || photoUrl.isBlank()) {
            throw new DdipBadRequestException("photoUrl 값이 없습니다.");
        }
        this.photos.add(Photo.builder()
                .photoUrl(photoUrl)
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(Instant.now())
                .status(PhotoStatus.PENDING)
                .responderComment(responderComment)
                .build());
        this.interactions.add(Interaction.builder()
                .actorId(responderId)
                .actorRole(ActorRole.RESPONDER)
                .actionType(ActionType.SUBMIT_PHOTO)
                .timestamp(Instant.now())
                .build());
        return this;
    }

    public DdipEvent updatePhotoFeedback(UUID requesterOrResponderId, UUID photoId, PhotoStatus status, String feedback) {
        if (status.equals(PhotoStatus.PENDING)) {
            throw new DdipForbiddenException("Pending 상태로 변경할수는 없습니다.");
        }
        Photo targetPhoto = findPhotoOrThrow(photoId);
        if (Objects.equals(requesterOrResponderId, this.requesterId)) { // 주체가 요청자
            if (status.equals(PhotoStatus.APPROVED)) {
                targetPhoto.approve();
            } else {
                boolean isQuestion = targetPhoto.feedbackByRequester(feedback);
                if (isQuestion) {
                    this.interactions.add(Interaction.builder()
                            .actorId(this.requesterId)
                            .actorRole(ActorRole.REQUESTER)
                            .actionType(ActionType.ASK_QUESTION)
                            .comment(feedback)
                            .relatedPhotoId(photoId)
                            .timestamp(Instant.now())
                            .build());
                } else {
                    this.interactions.add(Interaction.builder()
                            .actorId(this.requesterId)
                            .actorRole(ActorRole.REQUESTER)
                            .actionType(ActionType.REQUEST_REVISION)
                            .comment(feedback)
                            .relatedPhotoId(photoId)
                            .timestamp(Instant.now())
                            .build());
                }
            }
            return this;
        } else if (Objects.equals(requesterOrResponderId, this.selectedResponderId)) { // 주체가 수행자
            targetPhoto.feedbackByResponder(feedback);
            this.interactions.add(Interaction.builder()
                    .actorId(this.selectedResponderId)
                    .actorRole(ActorRole.RESPONDER)
                    .actionType(ActionType.ANSWER_QUESTION)
                    .comment(feedback)
                    .relatedPhotoId(photoId)
                    .timestamp(Instant.now())
                    .build());
            return this;
        } else
            throw new DdipBadRequestException("띱의 수행자 또는 요청자만 사진에 피드백을 남길 수 있습니다.");
    }

    public DdipEvent complete(UUID requesterId) {
        if (!Objects.equals(this.requesterId, requesterId)) {
            throw new DdipForbiddenException("띱을 등록한 사용자만 완료할 수 있습니다.");
        }
        if (photos.isEmpty()) {
            throw new DdipBadRequestException("업로드된 사진이 없습니다.");
        }
        Photo lastPhoto = photos.get(photos.size() - 1);
        if (lastPhoto.statusIsNotApproved()) {
            throw new DdipForbiddenException("최종 사진의 Status가 Approved가 아닙니다.");
        }
        this.status = DdipStatus.COMPLETED;
        this.interactions.add(Interaction.builder()
                .actorId(requesterId)
                .actorRole(ActorRole.RESPONDER)
                .actionType(ActionType.APPROVE)
                .timestamp(Instant.now())
                .build());
        return this;
    }

    public DdipEvent cancel(UUID requesterOrResponderId) {
        if (Objects.equals(requesterOrResponderId, this.requesterId)) { // 주체가 요청자
            this.status = DdipStatus.CANCELED;
            this.interactions.add(Interaction.builder()
                    .actorId(this.requesterId)
                    .actorRole(ActorRole.REQUESTER)
                    .actionType(ActionType.CANCEL_BY_REQUESTER)
                    .timestamp(Instant.now())
                    .build());
            return this;
        } else if (Objects.equals(requesterOrResponderId, this.selectedResponderId)) { // 주체가 수행자
            this.status = DdipStatus.CANCELED;
            this.interactions.add(Interaction.builder()
                    .actorId(this.selectedResponderId)
                    .actorRole(ActorRole.RESPONDER)
                    .actionType(ActionType.GIVE_UP_BY_RESPONDER)
                    .timestamp(Instant.now())
                    .build());
            return this;
        } else
            throw new DdipBadRequestException("띱의 수행자 또는 요청자만 취소할 수 있습니다.");
    }

    private Photo findPhotoOrThrow(UUID photoId) {
        return this.photos.stream()
                .filter(photo -> photo.getPhotoId().equals(photoId))
                .findFirst()
                .orElseThrow(() ->
                        new DdipNotFoundException("해당 ID를 가진 Photo가 존재하지 않습니다"));
    }
}

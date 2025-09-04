package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
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
    private final String cellId;
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

    public void apply(UUID applicantId) {
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
                    .build());
        }
    }

    public void selectResponder(UUID requesterId, UUID responderId) {
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
                .build());
    }
}

package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DdipEventTest {

    @DisplayName("띱 이벤트 생성 성공")
    @Test
    void givenDdipEventInfo_whenCreate_thenDdipEventIsCreated() {
        // given
        String title = "title";
        String content = "content";
        Integer reward = 1000;
        Double latitude = 35.888;
        Double longitude = 128.61;
        Integer difficulty = 3;
        UUID requesterId = UUID.randomUUID();

        // when
        DdipEvent ddipEvent = DdipEvent.create(title, content, reward, latitude, longitude, difficulty, requesterId);

        // then
        assertThat(ddipEvent.getTitle()).isEqualTo(title);
        assertThat(ddipEvent.getContent()).isEqualTo(content);
        assertThat(ddipEvent.getReward()).isEqualTo(reward);
        assertThat(ddipEvent.getLatitude()).isEqualTo(latitude);
        assertThat(ddipEvent.getLongitude()).isEqualTo(longitude);
        assertThat(ddipEvent.getDifficulty()).isEqualTo(difficulty);
        assertThat(ddipEvent.getRequesterId()).isEqualTo(requesterId);
        assertThat(ddipEvent.getStatus()).isEqualTo(DdipStatus.OPEN);
    }

    @DisplayName("띱 지원 성공")
    @Test
    void givenApplicantId_whenApply_thenApplicantIsAdded() {
        // given
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(UUID.randomUUID())
                .status(DdipStatus.OPEN)
                .applicants(new ArrayList<>())
                .interactions(new ArrayList<>())
                .build();
        UUID applicantId = UUID.randomUUID();

        // when
        ddipEvent.apply(applicantId);

        // then
        assertThat(ddipEvent.getApplicants()).contains(applicantId);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActorId()).isEqualTo(applicantId);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.APPLY);
    }

    @DisplayName("띱 지원 실패 - 마감된 띱")
    @Test
    void givenClosedDdipEvent_whenApply_thenDdipBadRequestExceptionIsThrown() {
        // given
        DdipEvent ddipEvent = DdipEvent.builder()
                .status(DdipStatus.IN_PROGRESS)
                .build();
        UUID applicantId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> ddipEvent.apply(applicantId))
                .isInstanceOf(DdipBadRequestException.class);
    }

    @DisplayName("띱 지원 실패 - 자신의 띱에 지원")
    @Test
    void givenRequesterIdAsApplicantId_whenApply_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .status(DdipStatus.OPEN)
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.apply(requesterId))
                .isInstanceOf(DdipBadRequestException.class);
    }

    @DisplayName("수행자 선택 성공")
    @Test
    void givenRequesterAndResponder_whenSelectResponder_thenResponderIsSelected() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        ArrayList<UUID> applicants = new ArrayList<>();
        applicants.add(responderId);

        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .status(DdipStatus.OPEN)
                .applicants(applicants)
                .interactions(new ArrayList<>())
                .build();

        // when
        ddipEvent.selectResponder(requesterId, responderId);

        // then
        assertThat(ddipEvent.getSelectedResponderId()).isEqualTo(responderId);
        assertThat(ddipEvent.getStatus()).isEqualTo(DdipStatus.IN_PROGRESS);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActorId()).isEqualTo(requesterId);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.SELECT_RESPONDER);
    }

    @DisplayName("수행자 선택 실패 - 요청자가 아님")
    @Test
    void givenNonRequester_whenSelectResponder_thenDdipForbiddenExceptionIsThrown() {
        // given
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(UUID.randomUUID())
                .build();
        UUID nonRequesterId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> ddipEvent.selectResponder(nonRequesterId, responderId))
                .isInstanceOf(DdipForbiddenException.class);
    }

    @DisplayName("수행자 선택 실패 - 이미 진행중인 띱")
    @Test
    void givenInProgressDdipEvent_whenSelectResponder_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .status(DdipStatus.IN_PROGRESS)
                .build();
        UUID responderId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> ddipEvent.selectResponder(requesterId, responderId))
                .isInstanceOf(DdipBadRequestException.class);
    }

    @DisplayName("수행자 선택 실패 - 지원자가 아님")
    @Test
    void givenNonApplicant_whenSelectResponder_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .status(DdipStatus.OPEN)
                .applicants(new ArrayList<>())
                .build();
        UUID nonApplicantId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> ddipEvent.selectResponder(requesterId, nonApplicantId))
                .isInstanceOf(DdipBadRequestException.class);
    }
}

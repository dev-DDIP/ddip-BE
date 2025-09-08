package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
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

    @DisplayName("사진 업로드 성공")
    @Test
    void givenPhotoInfo_whenUploadPhoto_thenPhotoIsUploaded() {
        // given
        UUID responderId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .selectedResponderId(responderId)
                .status(DdipStatus.IN_PROGRESS)
                .photos(new ArrayList<>())
                .interactions(new ArrayList<>())
                .build();
        String photoUrl = "https://example.com/photo.jpg";
        Double latitude = 35.888;
        Double longitude = 128.61;
        String responderComment = "사진 업로드 완료";

        // when
        ddipEvent.uploadPhoto(responderId, photoUrl, latitude, longitude, responderComment);

        // then
        assertThat(ddipEvent.getPhotos()).hasSize(1);
        Photo uploadedPhoto = ddipEvent.getPhotos().get(0);
        assertThat(uploadedPhoto.getPhotoUrl()).isEqualTo(photoUrl);
        assertThat(uploadedPhoto.getLatitude()).isEqualTo(latitude);
        assertThat(uploadedPhoto.getLongitude()).isEqualTo(longitude);
        assertThat(uploadedPhoto.getResponderComment()).isEqualTo(responderComment);
        assertThat(uploadedPhoto.getStatus()).isEqualTo(PhotoStatus.PENDING);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.SUBMIT_PHOTO);
    }

    @DisplayName("사진 업로드 실패 - 선택된 수행자가 아님")
    @Test
    void givenNonSelectedResponder_whenUploadPhoto_thenDdipForbiddenExceptionIsThrown() {
        // given
        UUID selectedResponderId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .selectedResponderId(selectedResponderId)
                .status(DdipStatus.IN_PROGRESS)
                .photos(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.uploadPhoto(otherUserId, "url", 35.888, 128.61, "comment"))
                .isInstanceOf(DdipForbiddenException.class);
    }

    @DisplayName("사진 피드백 업데이트 성공 - 요청자가 사진 승인")
    @Test
    void givenRequesterAndApprovedStatus_whenUpdatePhotoFeedback_thenPhotoIsApproved() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        Photo photo = Photo.builder()
                .photoId(photoId)
                .status(PhotoStatus.PENDING)
                .build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .photos(List.of(photo))
                .interactions(new ArrayList<>())
                .build();

        // when
        ddipEvent.updatePhotoFeedback(requesterId, photoId, PhotoStatus.APPROVED, null);

        // then
        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.APPROVED);
    }

    @DisplayName("사진 피드백 업데이트 성공 - 요청자가 질문 남김")
    @Test
    void givenRequesterAndFeedback_whenUpdatePhotoFeedback_thenQuestionIsAdded() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        Photo photo = Photo.builder()
                .photoId(photoId)
                .status(PhotoStatus.PENDING)
                .build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .photos(List.of(photo))
                .interactions(new ArrayList<>())
                .build();
        String feedback = "사진이 흐릿합니다";

        // when
        ddipEvent.updatePhotoFeedback(requesterId, photoId, PhotoStatus.REJECTED, feedback);

        // then
        assertThat(photo.getRequesterQuestion()).isEqualTo(feedback);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.ASK_QUESTION);
    }

    @DisplayName("사진 피드백 업데이트 성공 - 수행자가 답변 제공")
    @Test
    void givenResponderAndFeedback_whenUpdatePhotoFeedback_thenAnswerIsAdded() {
        // given
        UUID responderId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        Photo photo = Photo.builder()
                .photoId(photoId)
                .status(PhotoStatus.PENDING)
                .requesterQuestion("사진이 흐릿합니다")
                .build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .selectedResponderId(responderId)
                .photos(List.of(photo))
                .interactions(new ArrayList<>())
                .build();
        String answer = "다시 찍어서 올리겠습니다";

        // when
        ddipEvent.updatePhotoFeedback(responderId, photoId, PhotoStatus.REJECTED, answer);

        // then
        assertThat(photo.getResponderAnswer()).isEqualTo(answer);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.ANSWER_QUESTION);
    }

    @DisplayName("사진 피드백 업데이트 실패 - 권한 없는 사용자")
    @Test
    void givenUnauthorizedUser_whenUpdatePhotoFeedback_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        Photo photo = Photo.builder().photoId(photoId).build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .selectedResponderId(responderId)
                .photos(List.of(photo))
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.updatePhotoFeedback(unauthorizedUserId, photoId, PhotoStatus.APPROVED, null))
                .isInstanceOf(DdipBadRequestException.class);
    }

    @DisplayName("띱 완료 성공")
    @Test
    void givenApprovedPhoto_whenComplete_thenDdipEventIsCompleted() {
        // given
        UUID requesterId = UUID.randomUUID();
        Photo approvedPhoto = Photo.builder()
                .status(PhotoStatus.APPROVED)
                .build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .photos(List.of(approvedPhoto))
                .interactions(new ArrayList<>())
                .build();

        // when
        ddipEvent.complete(requesterId);

        // then
        assertThat(ddipEvent.getStatus()).isEqualTo(DdipStatus.COMPLETED);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.APPROVE);
    }

    @DisplayName("띱 완료 실패 - 요청자가 아님")
    @Test
    void givenNonRequester_whenComplete_thenDdipForbiddenExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.complete(otherUserId))
                .isInstanceOf(DdipForbiddenException.class);
    }

    @DisplayName("띱 완료 실패 - 업로드된 사진 없음")
    @Test
    void givenNoPhotos_whenComplete_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .photos(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.complete(requesterId))
                .isInstanceOf(DdipBadRequestException.class);
    }

    @DisplayName("띱 완료 실패 - 최종 사진이 승인되지 않음")
    @Test
    void givenUnapprovedLastPhoto_whenComplete_thenDdipForbiddenExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        Photo pendingPhoto = Photo.builder()
                .status(PhotoStatus.PENDING)
                .build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .photos(List.of(pendingPhoto))
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.complete(requesterId))
                .isInstanceOf(DdipForbiddenException.class);
    }

    @DisplayName("띱 취소 성공 - 요청자가 취소")
    @Test
    void givenRequester_whenCancel_thenDdipEventIsCanceledByRequester() {
        // given
        UUID requesterId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .interactions(new ArrayList<>())
                .build();

        // when
        ddipEvent.cancel(requesterId);

        // then
        assertThat(ddipEvent.getStatus()).isEqualTo(DdipStatus.CANCELED);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.CANCEL_BY_REQUESTER);
    }

    @DisplayName("띱 취소 성공 - 수행자가 포기")
    @Test
    void givenResponder_whenCancel_thenDdipEventIsCanceledByResponder() {
        // given
        UUID responderId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .selectedResponderId(responderId)
                .interactions(new ArrayList<>())
                .build();

        // when
        ddipEvent.cancel(responderId);

        // then
        assertThat(ddipEvent.getStatus()).isEqualTo(DdipStatus.CANCELED);
        assertThat(ddipEvent.getInteractions()).hasSize(1);
        assertThat(ddipEvent.getInteractions().get(0).getActionType()).isEqualTo(ActionType.GIVE_UP_BY_RESPONDER);
    }

    @DisplayName("띱 취소 실패 - 권한 없는 사용자")
    @Test
    void givenUnauthorizedUser_whenCancel_thenDdipBadRequestExceptionIsThrown() {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .requesterId(requesterId)
                .selectedResponderId(responderId)
                .build();

        // when & then
        assertThatThrownBy(() -> ddipEvent.cancel(unauthorizedUserId))
                .isInstanceOf(DdipBadRequestException.class);
    }
}

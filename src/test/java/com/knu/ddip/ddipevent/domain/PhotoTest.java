package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhotoTest {

    @DisplayName("사진 생성 성공")
    @Test
    void givenPhotoInfo_whenCreate_thenPhotoIsCreated() {
        // given
        UUID photoId = UUID.randomUUID();
        String photoUrl = "https://example.com/photo.jpg";
        Double latitude = 35.888;
        Double longitude = 128.61;
        Instant timestamp = Instant.now();
        String responderComment = "testComment";
        String requesterQuestion = "testQuestion";
        String responderAnswer = "testAnswer";
        String rejectionReason = "testReason";

        // when
        Photo photo = Photo.builder()
                .photoId(photoId)
                .photoUrl(photoUrl)
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(timestamp)
                .status(PhotoStatus.PENDING)
                .responderComment(responderComment)
                .requesterQuestion(requesterQuestion)
                .responderAnswer(responderAnswer)
                .rejectionReason(rejectionReason)
                .build();

        // then
        assertThat(photo.getPhotoId()).isEqualTo(photoId);
        assertThat(photo.getPhotoUrl()).isEqualTo(photoUrl);
        assertThat(photo.getLatitude()).isEqualTo(latitude);
        assertThat(photo.getLongitude()).isEqualTo(longitude);
        assertThat(photo.getTimestamp()).isEqualTo(timestamp);
        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.PENDING);
        assertThat(photo.getResponderComment()).isEqualTo(responderComment);
        assertThat(photo.getRequesterQuestion()).isEqualTo(requesterQuestion);
        assertThat(photo.getResponderAnswer()).isEqualTo(responderAnswer);
        assertThat(photo.getRejectionReason()).isEqualTo(rejectionReason);
    }

    @DisplayName("사진 승인 성공")
    @Test
    void givenPendingPhoto_whenApprove_thenStatusIsApproved() {
        // given
        Photo photo = Photo.builder().status(PhotoStatus.PENDING).build();

        // when
        photo.approve();

        // then
        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.APPROVED);
    }

    @DisplayName("사진 거절 성공")
    @Test
    void givenPendingPhoto_whenReject_thenStatusIsRejected() {
        // given
        Photo photo = Photo.builder().status(PhotoStatus.PENDING).build();

        // when
        photo.reject();

        // then
        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.REJECTED);
    }

    @DisplayName("요청자 피드백 - 첫 번째 질문 성공")
    @Test
    void givenFirstFeedback_whenFeedbackByRequester_thenQuestionIsSetAndReturnsTrue() {
        // given
        Photo photo = Photo.builder().status(PhotoStatus.PENDING).build();
        String feedback = "사진이 흐릿합니다";

        // when
        boolean isQuestion = photo.feedbackByRequester(feedback);

        // then
        assertThat(isQuestion).isTrue();
        assertThat(photo.getRequesterQuestion()).isEqualTo(feedback);
    }

    @DisplayName("요청자 피드백 - 답변 후 거절 성공")
    @Test
    void givenAnsweredQuestion_whenFeedbackByRequester_thenPhotoIsRejectedAndReturnsFalse() {
        // given
        Photo photo = Photo.builder()
                .status(PhotoStatus.PENDING)
                .requesterQuestion("기존 질문")
                .responderAnswer("수행자 답변")
                .build();
        String rejectionReason = "여전히 만족스럽지 않습니다";

        // when
        boolean isQuestion = photo.feedbackByRequester(rejectionReason);

        // then
        assertThat(isQuestion).isFalse();
        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.REJECTED);
        assertThat(photo.getRejectionReason()).isEqualTo(rejectionReason);
    }

    @DisplayName("요청자 피드백 실패 - 답변 없이 거절 시도")
    @Test
    void givenUnansweredQuestion_whenFeedbackByRequester_thenDdipBadRequestExceptionIsThrown() {
        // given
        Photo photo = Photo.builder()
                .status(PhotoStatus.PENDING)
                .requesterQuestion("기존 질문")
                .build();
        String feedback = "거절합니다";

        // when & then
        assertThatThrownBy(() -> photo.feedbackByRequester(feedback))
                .isInstanceOf(DdipBadRequestException.class)
                .hasMessageContaining("요청자가 남긴 질문에 대한 수행자의 응답 없이 사진을 반려할 수 없습니다");
    }

    @DisplayName("수행자 피드백 성공")
    @Test
    void givenQuestionExists_whenFeedbackByResponder_thenAnswerIsSet() {
        // given
        Photo photo = Photo.builder()
                .status(PhotoStatus.PENDING)
                .requesterQuestion("사진이 흐릿합니다")
                .build();
        String answer = "다시 찍어서 올리겠습니다";

        // when
        photo.feedbackByResponder(answer);

        // then
        assertThat(photo.getResponderAnswer()).isEqualTo(answer);
    }

    @DisplayName("수행자 피드백 실패 - 질문 없이 답변 시도")
    @Test
    void givenNoQuestion_whenFeedbackByResponder_thenDdipForbiddenExceptionIsThrown() {
        // given
        Photo photo = Photo.builder()
                .status(PhotoStatus.PENDING)
                .build();
        String answer = "답변입니다";

        // when & then
        assertThatThrownBy(() -> photo.feedbackByResponder(answer))
                .isInstanceOf(DdipForbiddenException.class)
                .hasMessageContaining("요청자의 질문 없이 질문에 대한 대답을 남길 수 없습니다");
    }

    @DisplayName("사진 상태 확인 - 승인되지 않음")
    @Test
    void givenNonApprovedPhoto_whenStatusIsNotApproved_thenReturnsTrue() {
        // given
        Photo pendingPhoto = Photo.builder().status(PhotoStatus.PENDING).build();
        Photo rejectedPhoto = Photo.builder().status(PhotoStatus.REJECTED).build();

        // when & then
        assertThat(pendingPhoto.statusIsNotApproved()).isTrue();
        assertThat(rejectedPhoto.statusIsNotApproved()).isTrue();
    }

    @DisplayName("사진 상태 확인 - 승인됨")
    @Test
    void givenApprovedPhoto_whenStatusIsNotApproved_thenReturnsFalse() {
        // given
        Photo approvedPhoto = Photo.builder().status(PhotoStatus.APPROVED).build();

        // when & then
        assertThat(approvedPhoto.statusIsNotApproved()).isFalse();
    }
}

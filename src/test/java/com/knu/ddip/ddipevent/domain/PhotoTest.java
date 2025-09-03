package com.knu.ddip.ddipevent.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}

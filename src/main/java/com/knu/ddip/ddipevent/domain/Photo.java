package com.knu.ddip.ddipevent.domain;

import com.knu.ddip.ddipevent.exception.DdipBadRequestException;
import com.knu.ddip.ddipevent.exception.DdipForbiddenException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Photo {
    private final UUID photoId;
    private final String photoUrl;
    private final Double latitude;
    private final Double longitude;
    private final Instant timestamp;
    private PhotoStatus status;
    private String responderComment;
    private String requesterQuestion;
    private String responderAnswer;
    private String rejectionReason;

    public void approve() {
        this.status = PhotoStatus.APPROVED;
    }

    public boolean feedbackByRequester(String feedback) {
        if (this.requesterQuestion == null) {
            this.requesterQuestion = feedback;
            return true;
        } else if (this.responderAnswer != null) {
            reject();
            this.rejectionReason = feedback;
            return false;
        } else throw new DdipBadRequestException("요청자가 남긴 질문에 대한 수행자의 응답 없이 사진을 반려할 수 없습니다.");
    }

    public void reject() {
        this.status = PhotoStatus.REJECTED;
    }

    public void feedbackByResponder(String feedback) {
        if (this.requesterQuestion == null) {
            throw new DdipForbiddenException("요청자의 질문 없이 질문에 대한 대답을 남길 수 없습니다.");
        }
        this.responderAnswer = feedback;
    }

    public boolean statusIsNotApproved() {
        return !this.status.equals(PhotoStatus.APPROVED);
    }
}

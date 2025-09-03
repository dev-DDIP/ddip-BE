package com.knu.ddip.ddipevent.domain;

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

    public void reject() {
        this.status = PhotoStatus.REJECTED;
    }
}

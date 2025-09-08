package com.knu.ddip.ddipevent.application.dto;

import com.knu.ddip.ddipevent.domain.PhotoStatus;

public record PhotoFeedbackRequest(
    PhotoStatus status,
    String feedback
) {
}

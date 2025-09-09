package com.knu.ddip.ddipevent.application.dto;

import java.util.UUID;

public record SelectApplicantRequest(
        UUID applicantId
) {
}

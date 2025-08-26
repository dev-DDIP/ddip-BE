package com.knu.ddip.ddipevent.application.dto;

public record UserSummaryDto(
        String userId,
        String nickname,
        String profileImageUrl,
        Double requesterRating,
        Integer requesterMissionCount,
        Double responderRating,
        Integer responderMissionCount,
        BadgeDto representativeBadge
) {
}

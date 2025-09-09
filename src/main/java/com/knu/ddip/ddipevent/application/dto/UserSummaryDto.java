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
    public static UserSummaryDto fromUserId(String userId) { // TODO : 구현
        return new UserSummaryDto(userId, null, null, null, null,
                null, null, null);
    }
}

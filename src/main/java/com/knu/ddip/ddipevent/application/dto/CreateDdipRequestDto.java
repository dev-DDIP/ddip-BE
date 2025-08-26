package com.knu.ddip.ddipevent.application.dto;

public record CreateDdipRequestDto(
        String title,
        String content,
        Integer reward,
        Double latitude,
        Double longitude,
        Integer difficulty
) {
}

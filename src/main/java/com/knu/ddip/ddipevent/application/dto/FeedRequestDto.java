package com.knu.ddip.ddipevent.application.dto;

public record FeedRequestDto(
        Double sw_lat,
        Double sw_lon,
        Double ne_lat,
        Double ne_lon,
        String sort,
        Double user_lat,
        Double user_lon
) {
}

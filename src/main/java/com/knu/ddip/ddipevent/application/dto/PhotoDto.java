package com.knu.ddip.ddipevent.application.dto;

import com.knu.ddip.ddipevent.domain.Photo;
import com.knu.ddip.ddipevent.domain.PhotoStatus;

public record PhotoDto(
        String photoId,
        String photoUrl,
        Double latitude,
        Double longitude,
        String timestamp,
        PhotoStatus status
) {
    public static PhotoDto fromEntity(Photo photo) {
        return new PhotoDto(
                photo.getPhotoId().toString(),
                photo.getPhotoUrl(),
                photo.getLatitude(),
                photo.getLongitude(),
                photo.getTimestamp().toString(),
                photo.getStatus()
        );
    }
}

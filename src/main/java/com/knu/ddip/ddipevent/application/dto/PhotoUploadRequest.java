package com.knu.ddip.ddipevent.application.dto;

import org.springframework.web.multipart.MultipartFile;

public record PhotoUploadRequest(
        MultipartFile photo,
        double latitude,
        double longitude,
        String responderComment
) {
}

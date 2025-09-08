package com.knu.ddip.common.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String directory);

    void deleteFile(String fileUrl);

    boolean exists(String fileUrl);
}

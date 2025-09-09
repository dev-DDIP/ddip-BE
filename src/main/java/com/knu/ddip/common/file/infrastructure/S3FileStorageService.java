package com.knu.ddip.common.file.infrastructure;

import com.knu.ddip.common.file.FileStorageException;
import com.knu.ddip.common.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        validateFile(file);

        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String key = String.format("%s/%s/%s", directory, dateDir, fileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);

            log.info("S3 파일 업로드 완료: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new FileStorageException("파일 업로드에 실패했습니다: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3 서비스 오류: {}", e.awsErrorDetails().errorMessage());
            throw new FileStorageException("S3 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String key = extractS3Key(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 완료: {}", fileUrl);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileUrl, e);
            throw new FileStorageException("파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        try {
            String key = extractS3Key(fileUrl);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 파일 존재 여부 확인 실패: {}", fileUrl, e);
            return false;
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        return String.format("%s_%s%s", timestamp, uuid, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    private String extractS3Key(String fileUrl) {
        if (fileUrl.contains(".s3.amazonaws.com/")) {
            return fileUrl.substring(fileUrl.indexOf(".s3.amazonaws.com/") + 18);
        }

        throw new IllegalArgumentException("올바르지 않은 S3 URL 형식입니다: " + fileUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("빈 파일은 업로드할 수 없습니다.");
        }

        // 파일 크기 제한 (50MB)
        long maxSize = 50 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new FileStorageException("파일 크기는 50MB를 초과할 수 없습니다.");
        }

        // 이미지 파일만 허용
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("이미지 파일만 업로드 가능합니다.");
        }
    }
}

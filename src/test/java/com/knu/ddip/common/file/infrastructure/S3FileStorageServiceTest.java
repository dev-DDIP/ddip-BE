package com.knu.ddip.common.file.infrastructure;

import com.knu.ddip.common.file.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @InjectMocks
    private S3FileStorageService s3FileStorageService;

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3FileStorageService, "bucketName", BUCKET_NAME);
    }

    @DisplayName("파일 업로드 성공")
    @Test
    void givenValidFile_whenUploadFile_thenFileUrlIsReturned() throws IOException {
        // given
        String originalFilename = "test.jpg";
        String contentType = "image/jpeg";
        long fileSize = 1024L;
        byte[] fileContent = "test content".getBytes();
        String directory = "photos";

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
        given(multipartFile.getContentType()).willReturn(contentType);
        given(multipartFile.getSize()).willReturn(fileSize);
        given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream(fileContent));
        given(multipartFile.isEmpty()).willReturn(false);

        PutObjectResponse putObjectResponse = PutObjectResponse.builder().build();
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(putObjectResponse);

        // when
        String result = s3FileStorageService.uploadFile(multipartFile, directory);

        // then
        assertThat(result).contains(BUCKET_NAME);
        assertThat(result).contains(directory);
        assertThat(result).startsWith("https://");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @DisplayName("파일 업로드 실패 - 빈 파일")
    @Test
    void givenEmptyFile_whenUploadFile_thenFileStorageExceptionIsThrown() {
        // given
        given(multipartFile.isEmpty()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("빈 파일은 업로드할 수 없습니다");
    }

    @DisplayName("파일 업로드 실패 - 파일 크기 초과")
    @Test
    void givenOversizedFile_whenUploadFile_thenFileStorageExceptionIsThrown() {
        // given
        long oversizedFileSize = 51 * 1024 * 1024; // 51MB
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getSize()).willReturn(oversizedFileSize);

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("파일 크기는 50MB를 초과할 수 없습니다");
    }

    @DisplayName("파일 업로드 실패 - 지원하지 않는 파일 타입")
    @Test
    void givenUnsupportedFileType_whenUploadFile_thenFileStorageExceptionIsThrown() {
        // given
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getSize()).willReturn(1024L);
        given(multipartFile.getContentType()).willReturn("text/plain");

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("이미지 파일만 업로드 가능합니다");
    }

    @DisplayName("파일 업로드 실패 - 콘텐츠 타입이 null")
    @Test
    void givenNullContentType_whenUploadFile_thenFileStorageExceptionIsThrown() {
        // given
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getSize()).willReturn(1024L);
        given(multipartFile.getContentType()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("이미지 파일만 업로드 가능합니다");
    }

    @DisplayName("파일 업로드 실패 - IOException")
    @Test
    void givenIOException_whenUploadFile_thenFileStorageExceptionIsThrown() throws IOException {
        // given
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getOriginalFilename()).willReturn("test.jpg");
        given(multipartFile.getContentType()).willReturn("image/jpeg");
        given(multipartFile.getSize()).willReturn(1024L);
        given(multipartFile.getInputStream()).willThrow(new IOException("IO Error"));

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("파일 업로드에 실패했습니다");
    }

    @DisplayName("파일 업로드 실패 - S3Exception")
    @Test
    void givenS3Exception_whenUploadFile_thenFileStorageExceptionIsThrown() throws IOException {
        // given
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getOriginalFilename()).willReturn("test.jpg");
        given(multipartFile.getContentType()).willReturn("image/jpeg");
        given(multipartFile.getSize()).willReturn(1024L);
        given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream("test".getBytes()));

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(RuntimeException.class);

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.uploadFile(multipartFile, "photos"))
                .isInstanceOf(RuntimeException.class);
    }

    @DisplayName("파일 삭제 성공")
    @Test
    void givenValidFileUrl_whenDeleteFile_thenFileIsDeleted() {
        // given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/photos/2023/12/01/test.jpg";
        DeleteObjectResponse deleteObjectResponse = DeleteObjectResponse.builder().build();
        given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willReturn(deleteObjectResponse);

        // when
        s3FileStorageService.deleteFile(fileUrl);

        // then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @DisplayName("파일 삭제 실패 - S3Exception")
    @Test
    void givenS3Exception_whenDeleteFile_thenFileStorageExceptionIsThrown() {
        // given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/photos/2023/12/01/test.jpg";
        given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .willThrow(RuntimeException.class);

        // when & then
        assertThatThrownBy(() -> s3FileStorageService.deleteFile(fileUrl))
                .isInstanceOf(RuntimeException.class);
    }

    @DisplayName("파일 존재 여부 확인 성공 - 파일 존재함")
    @Test
    void givenExistingFile_whenExists_thenReturnsTrue() {
        // given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/photos/2023/12/01/test.jpg";
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder().build();
        given(s3Client.headObject(any(HeadObjectRequest.class))).willReturn(headObjectResponse);

        // when
        boolean result = s3FileStorageService.exists(fileUrl);

        // then
        assertThat(result).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @DisplayName("파일 존재 여부 확인 성공 - 파일 존재하지 않음")
    @Test
    void givenNonExistingFile_whenExists_thenReturnsFalse() {
        // given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/photos/2023/12/01/nonexistent.jpg";
        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(mock(NoSuchKeyException.class));

        // when
        boolean result = s3FileStorageService.exists(fileUrl);

        // then
        assertThat(result).isFalse();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }
}

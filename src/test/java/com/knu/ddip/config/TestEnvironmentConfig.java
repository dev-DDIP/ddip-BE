package com.knu.ddip.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestEnvironmentConfig implements BeforeAllCallback {

    // JWT 테스트용 시크릿 키
    private static final String TEST_JWT_SECRET = "testSecretKeyForJwtTestingPurposesOnlyDoNotUseInProduction123456";

    // Redis 테스트용 설정값
    private static final String TEST_REDIS_HOST = "localhost";
    private static final String TEST_REDIS_PORT = "6379";
    private static final String TEST_REDIS_PASSWORD = "testpassword";

    // MySQL 테스트용 설정값
    private static final String TEST_MYSQL_HOST = "localhost";
    private static final String TEST_MYSQL_PORT = "3306";
    private static final String TEST_MYSQL_USER = "testuser";
    private static final String TEST_MYSQL_PASSWORD = "testpassword";
    private static final String TEST_MYSQL_DATABASE = "testdb";

    // OAuth 테스트용 설정값
    private static final String TEST_OAUTH_APP_REDIRECT_URI = "http://localhost:3000/test";

    // Kakao OAuth 테스트용 설정값
    private static final String TEST_KAKAO_REST_API_KEY = "test_kakao_api_key_for_testing_only";
    private static final String TEST_KAKAO_BACKEND_REDIRECT_URI = "http://localhost:8080/auth/oauth/kakao/callback/test";

    // S3 테스트용 설정값
    private static final String AWS_ACCESS_KEY_ID = "AKIA123456789TESTKEY";
    private static final String AWS_SECRET_ACCESS_KEY = "abc123xyz456def789ghi000testKeySecretValue";
    private static final String S3_BUCKET_NAME = "my-test-bucket";

    @Override
    public void beforeAll(ExtensionContext context) {
        // JWT 설정
        System.setProperty("SECRET_KEY", TEST_JWT_SECRET);

        // Redis 설정
        System.setProperty("REDIS_HOST", TEST_REDIS_HOST);
        System.setProperty("REDIS_PORT", TEST_REDIS_PORT);
        System.setProperty("REDIS_PASSWORD", TEST_REDIS_PASSWORD);

        // MySQL 설정
        System.setProperty("MYSQL_HOST", TEST_MYSQL_HOST);
        System.setProperty("MYSQL_PORT", TEST_MYSQL_PORT);
        System.setProperty("MYSQL_USER", TEST_MYSQL_USER);
        System.setProperty("MYSQL_PASSWORD", TEST_MYSQL_PASSWORD);
        System.setProperty("MYSQL_DATABASE", TEST_MYSQL_DATABASE);

        // OAuth 설정
        System.setProperty("OAUTH_APP_REDIRECT_URI", TEST_OAUTH_APP_REDIRECT_URI);

        // Kakao OAuth 설정
        System.setProperty("KAKAO_REST_API_KEY", TEST_KAKAO_REST_API_KEY);
        System.setProperty("KAKAO_BACKEND_REDIRECT_URI", TEST_KAKAO_BACKEND_REDIRECT_URI);

        // S3 설정
        System.setProperty("AWS_ACCESS_KEY_ID", AWS_ACCESS_KEY_ID);
        System.setProperty("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        System.setProperty("S3_BUCKET_NAME", S3_BUCKET_NAME);
    }
}

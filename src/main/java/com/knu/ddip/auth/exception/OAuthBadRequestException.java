package com.knu.ddip.auth.exception;

public class OAuthBadRequestException extends RuntimeException {
    public OAuthBadRequestException(String message) {
        super(message);
    }
}

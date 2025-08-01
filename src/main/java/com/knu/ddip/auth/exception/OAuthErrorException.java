package com.knu.ddip.auth.exception;

public class OAuthErrorException extends RuntimeException {
    public OAuthErrorException(String message) {
        super(message);
    }
}

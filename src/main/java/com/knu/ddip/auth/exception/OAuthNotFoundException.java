package com.knu.ddip.auth.exception;

public class OAuthNotFoundException extends RuntimeException {
    public OAuthNotFoundException(String message) {
        super(message);
    }
}

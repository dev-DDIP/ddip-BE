package com.knu.ddip.auth.exception;

public class TokenBadRequestException extends RuntimeException {
    public TokenBadRequestException(String message) {
        super(message);
    }
}

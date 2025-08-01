package com.knu.ddip.auth.exception;

public class TokenConflictException extends RuntimeException {
    public TokenConflictException(String message) {
        super(message);
    }
}

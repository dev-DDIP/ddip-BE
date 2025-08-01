package com.knu.ddip.auth.exception;

public class TokenStolenException extends RuntimeException {
    public TokenStolenException(String message) {
        super(message);
    }
}

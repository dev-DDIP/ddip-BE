package com.knu.ddip.user.exception;

public class UserEmailDuplicateException extends RuntimeException {
    public UserEmailDuplicateException(String message) {
        super(message);
    }
}

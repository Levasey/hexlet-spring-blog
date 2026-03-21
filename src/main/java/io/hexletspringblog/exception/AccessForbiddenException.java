package io.hexletspringblog.exception;

public class AccessForbiddenException extends RuntimeException {

    public AccessForbiddenException(String message) {
        super(message);
    }
}

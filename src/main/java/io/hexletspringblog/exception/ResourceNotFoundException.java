package io.hexletspringblog.exception;

// Имя класса исключения не принципиально
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

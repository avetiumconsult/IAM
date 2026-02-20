package com.aventium.identity_service.exception;

public class ApiExceptions {
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }
}

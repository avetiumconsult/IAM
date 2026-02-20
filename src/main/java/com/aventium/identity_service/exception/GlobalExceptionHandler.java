package com.aventium.identity_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.aventium.identity_service.exception.ApiExceptions.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex, HttpServletRequest req) {
        return error(req, HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException ex, HttpServletRequest req) {
        return error(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return error(req, HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return error(req, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));
        return error(req, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid request", details);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        String path = req.getRequestURI();
        String message = ex.getMessage();

        // Provide a clearer hint for the common /h2-console case, but still return a proper 404.
        if (path != null && path.startsWith("/h2-console")) {
            message = "H2 console endpoint is not available. Ensure spring.h2.console.enabled=true "
                    + "in your application configuration and that H2 is on the classpath.";
        }

        return error(req, HttpStatus.NOT_FOUND, "NOT_FOUND", message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex, HttpServletRequest req) {
        // Do not rethrow based on path: turning framework exceptions into RuntimeException
        // creates noisy logs and hides the real HTTP status (e.g., 404 for missing resources).
        return error(req, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> error(HttpServletRequest req, HttpStatus status, String code, String message, Map<String, ?> details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("path", req.getRequestURI());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        if (details != null) body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }
}

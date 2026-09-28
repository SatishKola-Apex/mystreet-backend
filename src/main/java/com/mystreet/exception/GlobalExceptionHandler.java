package com.mystreet.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("error", "NOT_FOUND");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("error", "FORBIDDEN");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("error", "BAD_REQUEST");
        errorBody.put("message", "Invalid request parameters");
        errorBody.put("details", validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("error", "INTERNAL_SERVER_ERROR");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}

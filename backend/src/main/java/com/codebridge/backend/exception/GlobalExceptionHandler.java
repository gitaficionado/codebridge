
package com.codebridge.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidArgument(
            IllegalArgumentException exception) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 400);
        response.put("message", "Invalid request");
        response.put("error", "Invalid GitHub repository URL");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<Map<String, Object>> handleGitHubApiException(
            GitHubApiException exception) {

        HttpStatus status;
        String message;

        switch (exception.getReason()) {
            case NOT_FOUND -> {
                status = HttpStatus.NOT_FOUND;
                message = "GitHub repository not found or not publicly accessible";
            }
            case RATE_LIMITED -> {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                message = "GitHub API rate limit reached. Please try again later.";
            }
            case UPSTREAM_ERROR -> {
                status = HttpStatus.BAD_GATEWAY;
                message = "Unable to retrieve information from GitHub";
            }
            default -> {
                status = HttpStatus.BAD_GATEWAY;
                message = "GitHub service unavailable";
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status.value());
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

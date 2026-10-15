package com.example.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {
        int status = exception.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        ApiErrorResponse body = new ApiErrorResponse(
                java.time.Instant.now(),
                status,
                httpStatus.getReasonPhrase(),
                exception.getReason(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}

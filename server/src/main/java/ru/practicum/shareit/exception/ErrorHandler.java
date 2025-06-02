package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidateException e) {
        log.info("400 {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("error", e.getMessage(), List.of());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(EmailException e) {
        log.info("409 {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("error", e.getMessage(), List.of());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse("error", e.getMessage(), List.of());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(NotFoundCustomException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundCustom(NotFoundCustomException e) {
        log.info("404 {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("error", e.getMessage(), List.of());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException e) {
        ErrorResponse errorResponse = new ErrorResponse("error", "Internal server error", List.of(e.getMessage()));
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(AccessException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessException e) {
        ErrorResponse errorResponse = new ErrorResponse("error", "Access denied", List.of());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
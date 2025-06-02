package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidateException e) {
        ErrorResponse errorResponse = new ErrorResponse("error", e.getMessage(), List.of());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(NotFoundCustomException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundCustomException ex) {
        return new ErrorResponse("error", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException e) {
        ErrorResponse errorResponse = new ErrorResponse("error", "Internal server error", List.of(e.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionDto> handleUnknownStateException(UnknownStateException e) {
        log.info("400 {}", e.getMessage());
        return new ResponseEntity<>(new ExceptionDto("Unknown state: UNSUPPORTED_STATUS"), HttpStatus.BAD_REQUEST);
    }

    @Getter
    @RequiredArgsConstructor
    private static class ExceptionDto {
        private final String error;
    }
}
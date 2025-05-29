package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

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

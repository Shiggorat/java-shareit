package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ErrorResponse {
    private String status;
    private String message;
    private List<String> errors;

    public ErrorResponse(String status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
}

package com.course.app.exception;

import java.util.Map;

import lombok.Getter;

@Getter
public class ValidationErrorResponse extends ErrorResponse {
    private final Map<String, String> errors;

    public ValidationErrorResponse(int status, String code, String message, Map<String, String> errors) {
        super(status, code, message);
        this.errors = errors;
    }
}

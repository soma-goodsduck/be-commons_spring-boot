package com.ducks.goodsduck.commons.model.dto;

import org.springframework.http.HttpStatus;

/** 모든 API 반환 형식의 통일을 위한 Error DTO */
public class ApiError {

    private final String message;

    private final int status;

    public ApiError(Throwable throwable, HttpStatus status) {
        this(throwable.getMessage(), status);
    }

    public ApiError(String errorMessage, HttpStatus status) {
        this.message = errorMessage;
        this.status = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
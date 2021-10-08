package com.ducks.goodsduck.commons.exception.user;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class InvalidJwtException extends ApplicationException {

    static final int ERROR_CODE = -201;
    static final String DEFAULT_MESSAGE = "Invalid jwt.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public InvalidJwtException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidJwtException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public InvalidJwtException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected InvalidJwtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

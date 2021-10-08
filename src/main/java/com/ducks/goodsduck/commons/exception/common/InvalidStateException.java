package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class InvalidStateException extends ApplicationException {

    static final int ERROR_CODE = -104;
    static final String DEFAULT_MESSAGE = "Request is not suitable for state of entity.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public InvalidStateException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidStateException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public InvalidStateException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected InvalidStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

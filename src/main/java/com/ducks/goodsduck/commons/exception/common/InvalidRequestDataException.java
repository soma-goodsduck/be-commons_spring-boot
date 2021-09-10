package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class InvalidRequestDataException extends ApplicationException {

    static final int ERROR_CODE = -103;
    static final String DEFAULT_MESSAGE = "Some request data are invalid or missing.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public InvalidRequestDataException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidRequestDataException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidRequestDataException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public InvalidRequestDataException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected InvalidRequestDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

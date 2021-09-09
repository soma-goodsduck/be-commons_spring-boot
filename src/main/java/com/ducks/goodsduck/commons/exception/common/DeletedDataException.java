package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class DeletedDataException extends ApplicationException {

    static final int ERROR_CODE = -105;
    static final String DEFAULT_MESSAGE = "Cannot access to deleted data.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public DeletedDataException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public DeletedDataException(String message) {
        super(message, ERROR_CODE);
    }

    public DeletedDataException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public DeletedDataException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected DeletedDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

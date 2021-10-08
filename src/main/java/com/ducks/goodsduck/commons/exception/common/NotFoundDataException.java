package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class NotFoundDataException extends ApplicationException {

    static final int ERROR_CODE = -101;
    static final String DEFAULT_MESSAGE = "Requested data does not exists.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public NotFoundDataException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public NotFoundDataException(String message) {
        super(message, ERROR_CODE);
    }

    public NotFoundDataException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public NotFoundDataException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected NotFoundDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

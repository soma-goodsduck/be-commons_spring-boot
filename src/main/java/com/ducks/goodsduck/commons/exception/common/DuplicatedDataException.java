package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class DuplicatedDataException extends ApplicationException {

    static final int ERROR_CODE = -102;
    static final String DEFAULT_MESSAGE = "Requested data already exists.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public DuplicatedDataException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public DuplicatedDataException(String message) {
        super(message, ERROR_CODE);
    }

    public DuplicatedDataException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public DuplicatedDataException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected DuplicatedDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

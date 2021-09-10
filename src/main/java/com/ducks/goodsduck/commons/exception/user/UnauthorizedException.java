package com.ducks.goodsduck.commons.exception.user;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class UnauthorizedException extends ApplicationException {

    static final int ERROR_CODE = -203;
    static final String DEFAULT_MESSAGE = "Unauthorized request. (inappropriate relationship)";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public UnauthorizedException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public UnauthorizedException(String message) {
        super(message, ERROR_CODE);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

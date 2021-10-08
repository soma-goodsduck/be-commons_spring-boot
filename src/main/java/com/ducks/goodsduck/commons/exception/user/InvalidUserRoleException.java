package com.ducks.goodsduck.commons.exception.user;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class InvalidUserRoleException extends ApplicationException {

    static final int ERROR_CODE = -202;
    static final String DEFAULT_MESSAGE = "Inappropriate role of user";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public InvalidUserRoleException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidUserRoleException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidUserRoleException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public InvalidUserRoleException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected InvalidUserRoleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

package com.ducks.goodsduck.commons.exception.user;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class SmsAuthorizationException extends ApplicationException {

    static final int ERROR_CODE = -205;
    static final String DEFAULT_MESSAGE = "Send of sms authorization failed. Check the coolsms server and settings.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public SmsAuthorizationException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public SmsAuthorizationException(String message) {
        super(message, ERROR_CODE);
    }

    public SmsAuthorizationException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public SmsAuthorizationException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected SmsAuthorizationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

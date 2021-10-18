package com.ducks.goodsduck.commons.exception.common;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class UploadRequestExceedException extends ApplicationException {

    static final int ERROR_CODE = -106;
    static final String DEFAULT_MESSAGE = "Too many upload requests.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public UploadRequestExceedException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public UploadRequestExceedException(String message) {
        super(message, ERROR_CODE);
    }

    public UploadRequestExceedException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public UploadRequestExceedException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected UploadRequestExceedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

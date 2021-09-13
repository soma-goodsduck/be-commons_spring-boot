package com.ducks.goodsduck.commons.exception.image;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class InvalidMetadataException extends ApplicationException {

    static final int ERROR_CODE = -402;
    static final String DEFAULT_MESSAGE = "Exception occured during reading metadata of image.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public InvalidMetadataException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidMetadataException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidMetadataException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public InvalidMetadataException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected InvalidMetadataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

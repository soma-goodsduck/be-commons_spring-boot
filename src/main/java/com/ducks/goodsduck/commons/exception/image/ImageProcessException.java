package com.ducks.goodsduck.commons.exception.image;

import com.ducks.goodsduck.commons.exception.ApplicationException;

public class ImageProcessException extends ApplicationException {

    static final int ERROR_CODE = -401;
    static final String DEFAULT_MESSAGE = "Exception occured during processing image.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public ImageProcessException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public ImageProcessException(String message) {
        super(message, ERROR_CODE);
    }

    public ImageProcessException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public ImageProcessException(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected ImageProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

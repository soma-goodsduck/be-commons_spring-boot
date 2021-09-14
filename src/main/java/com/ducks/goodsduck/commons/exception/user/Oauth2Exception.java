package com.ducks.goodsduck.commons.exception.user;

import com.ducks.goodsduck.commons.exception.ApplicationException;
import com.ducks.goodsduck.commons.model.enums.SocialType;

public class Oauth2Exception extends ApplicationException {

    static final int ERROR_CODE = -204;
    static final String DEFAULT_MESSAGE = "Disable to get information from Oauth2 Server.";

    public int getERROR_CODE() {
        return ERROR_CODE;
    }

    public Oauth2Exception(SocialType socialType) {
        super(DEFAULT_MESSAGE + " - " + socialType.toString(), ERROR_CODE);
    }

    public Oauth2Exception() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public Oauth2Exception(String message) {
        super(message, ERROR_CODE);
    }

    public Oauth2Exception(String message, Throwable cause) {
        super(message, cause, ERROR_CODE);
    }

    public Oauth2Exception(Throwable cause) {
        super(cause, ERROR_CODE);
    }

    protected Oauth2Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR_CODE);
    }
}

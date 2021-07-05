package com.ducks.goodsduck.commons.domain;

import lombok.Getter;

@Getter
public class AuthorizationNaver {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private String expires_in;
    private String scope;
//    private String refresh_token_expires_in;
    private String error_description;
}

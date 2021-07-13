package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

@Data
public class AuthorizationNaverDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String expiresIn;
    private String scope;
    private String errorDescription;
}

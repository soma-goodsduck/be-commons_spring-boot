package com.ducks.goodsduck.commons.domain;

import lombok.Getter;

@Getter
public class Oauth2AuthorizationCodeDto {

    private String code;
    private String state;
}

package com.ducks.goodsduck.commons.model.dto.oauth2;

import lombok.Data;

@Data
public class PublicKeyOfApple {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
}

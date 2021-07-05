package com.ducks.goodsduck.commons.model;

import lombok.Data;

@Data
public class SocialAccountDto {

    private String id;
    private String nickName;
    private String email;
    private String phoneNumber;
    private Boolean isExist;

    public SocialAccountDto(String id, Boolean isExist) {
        this.id = id;
        this.isExist = isExist;
    }
}

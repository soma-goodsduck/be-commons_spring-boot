package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

@Data
public class UserLoginResponse {

    Boolean emailSuccess;
    Boolean passwordSuccess;

    public UserLoginResponse(Boolean emailSuccess, Boolean passwordSuccess) {
        this.emailSuccess = emailSuccess;
        this.passwordSuccess = passwordSuccess;
    }
}

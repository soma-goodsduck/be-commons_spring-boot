package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String email;
    private String password;
}

package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

@Data
public class UserResetRequest {

    private String email;
    private String password;
}

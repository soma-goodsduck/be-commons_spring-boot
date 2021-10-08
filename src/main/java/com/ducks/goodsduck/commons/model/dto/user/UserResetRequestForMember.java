package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

@Data
public class UserResetRequestForMember {

    private String email;
    private String nowPassword;
    private String newPassword;
}

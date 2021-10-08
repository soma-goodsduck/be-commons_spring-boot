package com.ducks.goodsduck.commons.model.dto.sms;

import lombok.Data;

@Data
public class SmsAuthenticationResponse {

    boolean success;
    private String email;

    public SmsAuthenticationResponse(boolean success, String email) {
        this.success = success;
        this.email = email;
    }
}

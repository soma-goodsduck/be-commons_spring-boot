package com.ducks.goodsduck.commons.model.dto.sms;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SmsAuthenticationRequest {
    private String phoneNumber;
    private String authenticationNumber;
}

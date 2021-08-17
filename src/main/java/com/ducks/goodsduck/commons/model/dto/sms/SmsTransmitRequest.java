package com.ducks.goodsduck.commons.model.dto.sms;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SmsTransmitRequest {
    private String phoneNumber;
}

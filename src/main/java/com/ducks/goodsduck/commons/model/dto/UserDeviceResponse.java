package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDeviceResponse {

    private String uuid;

    public UserDeviceResponse(String uuid) {
        this.uuid = uuid;
    }
}

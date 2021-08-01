package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceResponse {

    private String uuid;

    public DeviceResponse(String uuid) {
        this.uuid = uuid;
    }
}

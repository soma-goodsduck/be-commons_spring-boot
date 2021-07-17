package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

@Data
public class JwtDto {
    private Long userId;

    public JwtDto(Long userId) {
        this.userId = userId;
    }
}

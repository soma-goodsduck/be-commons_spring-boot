package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckNicknameDto {

    LocalDateTime updatedAt;
    Boolean isSame;

    public CheckNicknameDto(LocalDateTime updatedAt, Boolean isSame) {
        this.updatedAt = updatedAt;
        this.isSame = isSame;
    }
}

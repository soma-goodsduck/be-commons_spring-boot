package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckNicknameDto {

    LocalDateTime updatedAt;
    Boolean isSame;
    Boolean sameBefore;

    public CheckNicknameDto(LocalDateTime updatedAt, Boolean isSame) {
        this.updatedAt = updatedAt;
        this.isSame = isSame;
    }

    public CheckNicknameDto(LocalDateTime updatedAt, Boolean isSame, Boolean sameBefore) {
        this.updatedAt = updatedAt;
        this.isSame = isSame;
        this.sameBefore = sameBefore;
    }
}

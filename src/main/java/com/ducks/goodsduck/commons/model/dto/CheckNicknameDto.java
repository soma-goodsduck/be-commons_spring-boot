package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckNicknameDto {

    LocalDateTime updatedAt;
    Boolean isSame;
    String nickName;

    public CheckNicknameDto(LocalDateTime updatedAt, Boolean isSame, String nickName) {
        this.updatedAt = updatedAt;
        this.isSame = isSame;
        this.nickName = nickName;
    }
}

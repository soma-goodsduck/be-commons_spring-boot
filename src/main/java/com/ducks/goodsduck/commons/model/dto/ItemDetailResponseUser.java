package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

@Data
public class ItemDetailResponseUser {

    private String nickname;

    public ItemDetailResponseUser(User user) {
        this.nickname = user.getNickName();
    }
}

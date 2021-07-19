package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

@Data
public class ItemDetailResponseUser {

    private String nickName;

    public ItemDetailResponseUser(User user) {
        this.nickName = user.getNickName();
    }
}

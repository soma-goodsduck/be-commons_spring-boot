package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.user.UserIdolGroupDto;
import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemDetailResponseItemOwner {

    private Long userId;
    private String nickName;
    private String imageUrl;

    public ItemDetailResponseItemOwner(User user) {
        this.userId = user.getId();
        this.nickName = user.getNickName();
        this.imageUrl = user.getImageUrl();
    }
}

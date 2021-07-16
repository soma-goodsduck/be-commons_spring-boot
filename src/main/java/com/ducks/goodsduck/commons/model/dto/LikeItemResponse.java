package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.UserItem;
import lombok.Data;

@Data
public class LikeItemResponse {

    private Long userId;
    private Long itemId;
    private boolean isLike;

    public LikeItemResponse() {
        this.isLike = false;
    }

    public LikeItemResponse(Long userId, Long itemId) {
        this.userId = userId;
        this.itemId = itemId;
        this.isLike = false;
    }

    public LikeItemResponse(UserItem userItem) {
        this.userId = userItem.getUser().getId();
        this.itemId = userItem.getItem().getId();
        this.isLike = true;
    }
}

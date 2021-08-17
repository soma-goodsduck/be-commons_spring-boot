package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

@Data
public class PostDetailResponsePostOwner {

    private Long userId;
    private String bcryptId;
    private String nickName;
    private String imageUrl;

    public PostDetailResponsePostOwner(User user) {
        this.userId = user.getId();
        this.bcryptId = user.getBcryptId();
        this.nickName = user.getNickName();
        this.imageUrl = user.getImageUrl();
    }
}

package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSimpleDto {

    private String nickName;
    private String imageUrl;

    public UserSimpleDto(User user) {
        this.nickName = user.getNickName();
        this.imageUrl = user.getImageUrl();
    }
}

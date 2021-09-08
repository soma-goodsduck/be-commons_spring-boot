package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSimpleDto {

    private Long userId;
    private UserRole role;
    private String bcryptId;
    private String nickName;
    private String imageUrl;

    public UserSimpleDto(User user) {
        this.userId = user.getId();
        this.role = user.getRole();
        this.bcryptId = user.getBcryptId();
        this.nickName = user.getNickName();
        this.imageUrl = user.getImageUrl();
    }
}

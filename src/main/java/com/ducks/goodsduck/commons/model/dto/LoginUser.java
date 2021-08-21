package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.user.UserIdolGroupDto;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LoginUser {

    private Long userId;
    private String nickName;
    private List<UserIdolGroupDto> likeIdolGroups = new ArrayList<>();

    public LoginUser(User user) {
        this.userId = user.getId();
        this.nickName = user.getNickName();
        this.likeIdolGroups = user.getUserIdolGroups().stream()
                    .map(userIdolGroup -> new UserIdolGroupDto(userIdolGroup))
                    .collect(Collectors.toList());
    }
}

package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.item.ItemSimpleDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserChatDto {

    private List<UserSimpleDto> users = new ArrayList<>();
    private ItemSimpleDto item;

    public UserChatDto(List<User> users, Item item) {
        this.users = users.stream()
                    .map(user -> new UserSimpleDto(user))
                    .collect(Collectors.toList());
        this.item = new ItemSimpleDto(item);
    }
}

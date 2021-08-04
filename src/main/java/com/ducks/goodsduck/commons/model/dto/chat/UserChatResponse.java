package com.ducks.goodsduck.commons.model.dto.chat;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.UserChat;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserChatResponse {

    private String chatId;
    private UserSimpleDto otherUser;

    public UserChatResponse(UserChat userChat) {
        this.chatId = userChat.getChat().getId();
        this.otherUser = new UserSimpleDto(userChat.getUser());
    }
}

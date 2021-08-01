package com.ducks.goodsduck.commons.model.dto.chat;

import com.ducks.goodsduck.commons.model.dto.item.ItemSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Chat;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;

@Data
public class ChatAndItemDto {

    private String chatId;
    private ItemSimpleDto itemSimpleDto;

    public ChatAndItemDto(Chat chat, Item item) {
        this.chatId = chat.getId();
        this.itemSimpleDto = new ItemSimpleDto(item);
    }
}

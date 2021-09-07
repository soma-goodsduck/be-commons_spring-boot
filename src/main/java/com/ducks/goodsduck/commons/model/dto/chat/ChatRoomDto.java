package com.ducks.goodsduck.commons.model.dto.chat;

import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.entity.Chat;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;

@Data
public class ChatRoomDto {

    private String chatId;
    private ItemSummaryDto itemSimpleDto;
    private String senderNickName;

    public ChatRoomDto(Chat chat, Item item) {
        this.chatId = chat.getId();
        this.itemSimpleDto = new ItemSummaryDto(item);
    }

    public ChatRoomDto(Chat chat, Item item, String senderNickName) {
        this.chatId = chat.getId();
        this.itemSimpleDto = new ItemSummaryDto(item);
        this.senderNickName = senderNickName;
    }
}

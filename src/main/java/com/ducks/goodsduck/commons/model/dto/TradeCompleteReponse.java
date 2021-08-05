package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TradeCompleteReponse {

    private ItemSimpleDto item;
    private List<UserChatResponse> chatRooms;

    public TradeCompleteReponse(Item item, List<UserChatResponse> chatRooms) {
        this.item = new ItemSimpleDto(item);
        this.chatRooms = chatRooms;
    }
}

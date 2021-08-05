package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TradeCompleteReponse {

    private ItemSummaryDto item;
    private List<UserChatResponse> chatRooms;

    public  TradeCompleteReponse(Item item, List<UserChatResponse> chatRooms) {
        this.item = ItemSummaryDto.of(item);
        this.chatRooms = chatRooms;
    }
}

package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TradeCompleteReponse {

    private ItemSummaryDto item;
    private List<UserChatResponse> chatRooms;

    public TradeCompleteReponse(Item item, List<UserChatResponse> chatRooms) {
        this.item = ItemSummaryDto.of(item);

        // HINT: 리뷰 작성 단계에 프론트 단에서의 UI 처리를 위한 설정값
        this.item.setTradeStatus(TradeStatus.REVIEW);
        this.chatRooms = chatRooms;
    }
}

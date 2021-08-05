package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.item.ItemSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TradeCompleteReponse {

    private ItemSimpleDto item;
    private List<ReviewResponse> reviewResponses;

    public TradeCompleteReponse(Item item, List<ReviewResponse> reviewResponses) {
        this.item = new ItemSimpleDto(item);
        this.reviewResponses = reviewResponses;
    }
}

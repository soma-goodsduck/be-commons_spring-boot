package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MypageResponse {

    private Long countOfLikes;
    private Long countOfReceivedReviews;
    private Long countOfReceievedPriceProposes;
    private List<ItemSummaryDto> items;

    public MypageResponse(List<ItemSummaryDto> items) {
        this.items = items;
    }
}

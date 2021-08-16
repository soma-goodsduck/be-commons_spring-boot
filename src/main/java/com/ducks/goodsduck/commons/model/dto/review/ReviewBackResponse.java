package com.ducks.goodsduck.commons.model.dto.review;

import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Review;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewBackResponse {

    private ItemSummaryDto item;
    private ReviewResponse review;
    private String chatRoomId;

    public ReviewBackResponse(Item item, Review review, String chatRoomId) {
        this.item = ItemSummaryDto.of(item);
        this.review = new ReviewResponse(review);
        this.chatRoomId = chatRoomId;
    }
}

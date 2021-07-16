package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PriceProposeResponse {

    private Long userId;
    private Long itemId;
    private PriceProposeStatus status = PriceProposeStatus.SUGGERSTED;
    private boolean price;

    public PriceProposeResponse(Long userId, Long itemId, boolean price) {
        this.userId = userId;
        this.itemId = itemId;
        this.price = price;
    }
}

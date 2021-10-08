package com.ducks.goodsduck.commons.model.dto.pricepropose;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PriceProposeSimpleDto {

    private Long priceProposeId;
    private int proposedPrice;
    private PriceProposeStatus status;
    private LocalDateTime createdAt;

    public PriceProposeSimpleDto(PricePropose pricePropose) {
        this.priceProposeId = pricePropose.getId();
        this.proposedPrice = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.createdAt = pricePropose.getCreatedAt();
    }

    public PriceProposeSimpleDto(User user, Item item, PricePropose pricePropose) {
        this.priceProposeId = pricePropose.getId();
        this.proposedPrice = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.createdAt = pricePropose.getCreatedAt();
    }
}

package com.ducks.goodsduck.commons.model.dto.pricepropose;

import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PriceProposeResponse {

    private Long priceProposeId;
    private Long receiverId;
    private UserSimpleDto proposer;
    private ItemSummaryDto item;
    private int proposedPrice;
    private PriceProposeStatus status;
    private LocalDateTime createdAt;

    public PriceProposeResponse (PricePropose pricePropose) {
        this.priceProposeId = pricePropose.getId();
        this.receiverId = pricePropose.getItem().getUser().getId();
        this.proposer = new UserSimpleDto();
        this.item = new ItemSummaryDto();
        this.proposedPrice = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.createdAt = pricePropose.getCreatedAt();
    }

    public PriceProposeResponse (User user, Item item, PricePropose pricePropose) {
        this.priceProposeId = pricePropose.getId();
        this.receiverId = item.getUser().getId();
        this.proposer = new UserSimpleDto(user);
        this.item = new ItemSummaryDto(item);
        this.proposedPrice = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.createdAt = pricePropose.getCreatedAt();
    }
}

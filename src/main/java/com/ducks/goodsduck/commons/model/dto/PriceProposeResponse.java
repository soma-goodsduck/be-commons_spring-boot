package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PriceProposeResponse {

    private Long priceProposeId;
    private UserSimpleDto proposer;
    private int price;
    private PriceProposeStatus status;
    private boolean isSuccess;

    public PriceProposeResponse (PricePropose pricePropose, boolean isSuccess) {
        this.priceProposeId = pricePropose.getId();
        this.proposer = new UserSimpleDto();
        this.price = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.isSuccess = isSuccess;
    }

    public PriceProposeResponse (User user, PricePropose pricePropose, boolean isSuccess) {
        this.priceProposeId = pricePropose.getId();
        this.proposer = new UserSimpleDto(user);
        this.price = pricePropose.getPrice();
        this.status = pricePropose.getStatus();
        this.isSuccess = isSuccess;
    }
}

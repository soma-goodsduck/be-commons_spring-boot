package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PriceProposeResponse {

    private Long priceProposeId;
    private PriceProposeStatus status;
    private int price;
    private boolean isSuccess;

    public PriceProposeResponse (PricePropose pricePropose, boolean isSuccess) {
        this.priceProposeId = pricePropose.getId();
        this.status = pricePropose.getStatus();
        this.price = pricePropose.getPrice();
        this.isSuccess = isSuccess;
    }
}

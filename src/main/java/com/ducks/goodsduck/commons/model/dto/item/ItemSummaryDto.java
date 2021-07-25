package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ItemSummaryDto {

    private Long itemId;
    private String name;
    private Long price;
    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private LocalDateTime itemCreatedAt;
    private ImageDto image;

    public static ItemSummaryDto of (Item item, ImageDto image) {
        return new ItemSummaryDto(item, image);
    }

    public static ItemSummaryDto of (Item item) {
        return new ItemSummaryDto(item);
    }

    public ItemSummaryDto(Item item) {
        this.itemId = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.tradeStatus = item.getTradeStatus();
        this.itemCreatedAt = item.getCreatedAt();
    }

    public ItemSummaryDto(Item item, ImageDto image) {
        this.itemId = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.tradeStatus = item.getTradeStatus();
        this.itemCreatedAt = item.getCreatedAt();
        this.image = image;
    }
}

package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ItemSummaryDto {

    private Long itemId;
    private String imageUrl;
    private String name;
    private Long price;
    private String tradeType;
    private String tradeStatus;
    private LocalDateTime itemCreatedAt;

    public static ItemSummaryDto of (Item item, String imageUrl) {
        return new ItemSummaryDto(item, imageUrl);
    }

    public static ItemSummaryDto of (Item item) {
        return new ItemSummaryDto(item);
    }

    public ItemSummaryDto(Item item) {
        this.itemId = item.getId();
        this.imageUrl = item.getImages().get(0).getUrl();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType().getKorName();
        this.tradeStatus = item.getTradeStatus().getKorName();
        this.itemCreatedAt = item.getCreatedAt();
    }

    public ItemSummaryDto(Item item, String imageUrl) {
        this.itemId = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType().getKorName();
        this.tradeStatus = item.getTradeStatus().getKorName();
        this.itemCreatedAt = item.getCreatedAt();
        this.imageUrl = imageUrl;
    }
}

package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.CategoryItem;
import com.ducks.goodsduck.commons.model.entity.IdolMember;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.StatusGrade;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDto {

    private Long id;
    private String name;
    private int price;
    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private StatusGrade statusGrade;
    private String imageUrl;
    private String description;
    private int views;
    private int likesItemCount;
    private LocalDateTime itemCreatedAt;
    private LocalDateTime updatedAt;
    private boolean isLike = false;
    private IdolMember idolMember;
    private UserSimpleDto userSimpleDto;
    private CategoryItem categoryItem;

    public ItemDto likesOfMe() {
        this.isLike = true;
        return this;
    }

    public ItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.tradeStatus = item.getTradeStatus();
        this.statusGrade = item.getStatusGrade();
        this.imageUrl = item.getImageUrl();
        this.description = item.getDescription();
        this.views = item.getViews();
        this.itemCreatedAt = item.getItemCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        this.likesItemCount = item.getLikesItemCount();
    }
}

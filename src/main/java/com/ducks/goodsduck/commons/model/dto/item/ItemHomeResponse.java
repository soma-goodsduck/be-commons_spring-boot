package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemHomeResponse {

    private UserSimpleDto itemOwner;
    private Long itemId;
    private String name;
    private String imageUrl;
    private Long price;
    private String tradeType;
    private TradeStatus tradeStatus;
    private ItemDetailResponseIdol idolMember;
    private LocalDateTime itemCreatedAt;
    private Integer views;
    private Integer likesItemCount;
    private Boolean isLike;

    public ItemHomeResponse(Item item) {
        this.itemOwner = new UserSimpleDto(item.getUser());
        this.itemId = item.getId();
        this.name = item.getName();
        if(!item.getImages().isEmpty()) this.imageUrl = item.getImages().get(0).getUrl();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType().getKorName();
        this.tradeStatus = item.getTradeStatus();
        this.idolMember = new ItemDetailResponseIdol(item.getIdolMember());
        this.itemCreatedAt = item.getCreatedAt();
        this.views = item.getViews();
        this.likesItemCount = item.getLikesItemCount();
        this.isLike = false;
    }

    public ItemHomeResponse(Item item, String imageUrl) {
        this.itemOwner = new UserSimpleDto(item.getUser());
        this.itemId = item.getId();
        this.name = item.getName();
        this.imageUrl = imageUrl;
        this.price = item.getPrice();
        this.tradeType = item.getTradeType().getKorName();
        this.tradeStatus = item.getTradeStatus();
        this.idolMember = new ItemDetailResponseIdol(item.getIdolMember());
        this.itemCreatedAt = item.getCreatedAt();
        this.views = item.getViews();
        this.likesItemCount = item.getLikesItemCount();
        this.isLike = false;
    }

    public void likesOfMe() {
        isLike = true;
    }
}
package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemHomeResponse {

    private ItemDetailResponseItemOwner itemOwner;
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
        this.itemOwner = new ItemDetailResponseItemOwner(item.getUser());
        this.itemId = item.getId();
        this.name = item.getName();
        // TODO : yml 반영
        this.imageUrl = "https://goodsduck-item-image.s3.ap-northeast-2.amazonaws.com/home-" + item.getImages().get(0).getUploadName();
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
        this.itemOwner = new ItemDetailResponseItemOwner(item.getUser());
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
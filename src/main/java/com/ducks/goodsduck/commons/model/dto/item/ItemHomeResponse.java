package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemHomeResponse {

    private ItemDetailResponseItemOwner itemOwner;
    private Long itemId;
    private String name;
//    private String imageUrl;
    private List<ItemDetailResponseImage> images = new ArrayList<>();
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
//        this.imageUrl = item.getImages().get(0).getUrl(); // TODO : 추후 변경 예정
        this.images = item.getImages().stream()
                .map(image -> new ItemDetailResponseImage(image))
                .collect(Collectors.toList());
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
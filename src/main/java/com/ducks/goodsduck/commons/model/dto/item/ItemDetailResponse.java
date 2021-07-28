package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemDetailResponse {

    private Long userId;
    private ItemDetailResponseItemOwner itemOwner;
    private Long itemId;
    private String name;
    private String description;
    private List<ItemDetailResponseImage> images = new ArrayList<>();
    private Long price;
    private String tradeType;
    private TradeStatus tradeStatus;
    private GradeStatus gradeStatus;
    private ItemDetailResponseIdol idolMember;
    private LocalDateTime itemCreatedAt;
    private String categoryName;
    private Integer views;
    private Integer likesItemCount;
    private Boolean isLike;
    private Boolean isOwner;

    public ItemDetailResponse(Item item) {
        this.itemId = item.getId();
        this.itemOwner = new ItemDetailResponseItemOwner(item.getUser());
        this.itemId = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.images = item.getImages().stream()
                        .map(image -> new ItemDetailResponseImage(image))
                        .collect(Collectors.toList());
        this.price = item.getPrice();
        this.tradeType = item.getTradeType().getKorName();
        this.tradeStatus = item.getTradeStatus();
        this.gradeStatus = item.getGradeStatus();
        this.idolMember = new ItemDetailResponseIdol(item.getIdolMember());
        this.itemCreatedAt = item.getCreatedAt();
        this.categoryName = item.getCategoryItem().getName();
        this.views = item.getViews();
        this.likesItemCount = item.getLikesItemCount();
        this.isLike = false;
        this.isOwner = false;
    }

    public void likesOfMe() {
        isLike = true;
    }

    public void myItem() {
        isOwner = true;
    }
}

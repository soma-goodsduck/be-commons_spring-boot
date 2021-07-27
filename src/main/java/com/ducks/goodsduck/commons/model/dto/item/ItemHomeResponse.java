package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemHomeResponse {

    private UserSimpleDto itemOwner;
    private Long itemId;
    private String name;
    private String description;
    private ItemDetailResponseImage image;
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

    public ItemHomeResponse(Item item) {
        this.itemId = item.getId();
        this.itemOwner = new UserSimpleDto(item.getUser());
        this.itemId = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
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
    }

    public void likesOfMe() {
        isLike = true;
    }
}

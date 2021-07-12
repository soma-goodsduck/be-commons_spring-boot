package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.StatusGrade;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

/**
 *  아이템 상세 페이지에 넘길 Item에 대한 DTO 클래스
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ItemDetailResponse {

    //TODO USER 엔티티와의 관계 포함

    //TODO IDOLMEMBER 엔티티와의 관계 포함

    //TODO CATEGORY_ITEM 엔티티와의 관계 포함

    private String name;
    private int price;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradetStatus;

    @Enumerated(EnumType.STRING)
    private StatusGrade statusGrade;

    private String imageUrl;
    private String description;
    private LocalDateTime itemCreatedAt;
    private LocalDateTime updatedAt;
    private int likesItemCount;

    public ItemDetailResponse(Item item) {
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.tradetStatus = item.getTradetStatus();
        this.imageUrl = item.getImageUrl();
        this.description = item.getDescription();
        this.itemCreatedAt = item.getItemCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        this.likesItemCount = item.getLikesItemCount();
    }

}

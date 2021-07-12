package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.enums.StatusGrade;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class ItemUploadRequest {

    //TODO USER 엔티티와의 관계 포함

    //TODO IDOLMEMBER 엔티티와의 관계 포함

    //TODO CATEGORY_ITEM 엔티티와의 관계 포함

    private String name;
    private int price;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    private StatusGrade statusGrade;

    private String imageUrl;
    private String description;

}

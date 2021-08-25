package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemFilterDto {

    private Long idolGroupId;
    private List<Long> idolMembersId = new ArrayList<>();
    private TradeType tradeType;
    private Long itemCategoryId;
    private GradeStatus gradeStatus;
    private Long minPrice;
    private Long maxPrice;

    public ItemFilterDto(Long idolGroupId, List<Long> idolMembersId, TradeType tradeType, Long itemCategoryId, GradeStatus gradeStatus, Long minPrice, Long maxPrice) {
        this.idolGroupId = idolGroupId;
        this.idolMembersId = idolMembersId;
        this.tradeType = tradeType;
        this.itemCategoryId = itemCategoryId;
        this.gradeStatus = gradeStatus;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}

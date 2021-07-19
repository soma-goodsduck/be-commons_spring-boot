package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

@Data
public class ItemUpdateRequest {

    // TODO: 유저가 이미지 변경(추가, 삭제)할 경우
    private String name;
    private Long price;
    private TradeType tradeType;
    private GradeStatus gradeStatus;
    private Long idolMember;

    // TODO: category -> categoryName
    private String category;
    private String description;
}

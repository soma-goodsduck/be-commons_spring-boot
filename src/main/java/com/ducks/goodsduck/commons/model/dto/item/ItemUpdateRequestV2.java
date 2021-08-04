package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

import java.util.List;

@Data
public class ItemUpdateRequestV2 {

    private String name;
    private Long price;
    private TradeType tradeType;
    private GradeStatus gradeStatus;
    private Long idolMember;
    private String category;
    private String description;
    private List<String> imageUrls;
}

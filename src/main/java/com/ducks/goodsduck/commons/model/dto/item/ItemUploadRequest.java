package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ItemUploadRequest {

    private String name;
    private Long price;
    private TradeType tradeType;
    private GradeStatus gradeStatus;
    private Long idolMember;
    private String category;
    private String description;
}

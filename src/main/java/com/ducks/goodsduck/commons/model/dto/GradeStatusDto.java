package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import lombok.Data;

@Data
public class GradeStatusDto {

    private String gradeStatus;
    private String description;

    public GradeStatusDto(GradeStatus gradeStatus) {
        this.gradeStatus = gradeStatus.name();
        this.description = gradeStatus.getDescription();
    }
}

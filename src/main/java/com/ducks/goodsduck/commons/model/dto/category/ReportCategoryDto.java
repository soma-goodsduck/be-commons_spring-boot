package com.ducks.goodsduck.commons.model.dto.category;

import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.Data;

@Data
public class ReportCategoryDto {

    private Long reportCategoryId;
    private String reportCategoryName;

    public ReportCategoryDto(Category category) {
        this.reportCategoryId = category.getId();
        this.reportCategoryName = category.getName();
    }
}

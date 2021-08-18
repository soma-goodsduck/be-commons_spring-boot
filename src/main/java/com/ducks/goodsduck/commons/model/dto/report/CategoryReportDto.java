package com.ducks.goodsduck.commons.model.dto.report;

import com.ducks.goodsduck.commons.model.entity.CategoryReport;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryReportDto {

    private String content;

    public CategoryReportDto(String content) {
        this.content = content;
    }

    public CategoryReportDto(CategoryReport categoryReport) {
        this.content = categoryReport.getType();
    }
}

package com.ducks.goodsduck.commons.model.dto.report;

import com.ducks.goodsduck.commons.model.entity.CategoryReport;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryReportAddRequest {

    private String content;

    public CategoryReportAddRequest(String content) {
        this.content = content;
    }

    public CategoryReportAddRequest(CategoryReport categoryReport) {
        this.content = categoryReport.getType();
    }
}

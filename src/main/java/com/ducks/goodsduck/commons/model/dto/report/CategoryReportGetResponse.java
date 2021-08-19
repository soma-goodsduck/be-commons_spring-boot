package com.ducks.goodsduck.commons.model.dto.report;

import com.ducks.goodsduck.commons.model.entity.CategoryReport;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryReportGetResponse {

    private Long id;
    private String content;

    public CategoryReportGetResponse(Long categoryReportId, String content) {
        this.id = categoryReportId;
        this.content = content;
    }

    public CategoryReportGetResponse(CategoryReport categoryReport) {
        this.id = categoryReport.getId();
        this.content = categoryReport.getType();
    }
}

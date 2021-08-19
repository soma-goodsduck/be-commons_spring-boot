package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CategoryReportResponse {

    private String receiverNickName;
    private List<CategoryReportGetResponse> categoryReports;

    public CategoryReportResponse(String receiverNickName, List<CategoryReportGetResponse> categoryReports) {
        this.receiverNickName = receiverNickName;
        this.categoryReports = categoryReports;
    }
}

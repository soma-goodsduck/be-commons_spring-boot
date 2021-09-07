package com.ducks.goodsduck.commons.model.dto.category;

import lombok.Data;

import java.util.List;

@Data
public class ReportCategoryResponse {

    private String receiverNickName;
    private List<CategoryResponse> reportCategoryList;

    public ReportCategoryResponse(String receiverNickName, List<CategoryResponse> reportCategoryList) {
        this.receiverNickName = receiverNickName;
        this.reportCategoryList = reportCategoryList;
    }
}

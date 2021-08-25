package com.ducks.goodsduck.commons.model.dto.category;

import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryDto;
import lombok.Data;

import java.util.List;

@Data
public class ReportCategoryResponse {

    private String receiverNickName;
    private List<ReportCategoryDto> reportCategoryList;

    public ReportCategoryResponse(String receiverNickName, List<ReportCategoryDto> reportCategoryList) {
        this.receiverNickName = receiverNickName;
        this.reportCategoryList = reportCategoryList;
    }
}

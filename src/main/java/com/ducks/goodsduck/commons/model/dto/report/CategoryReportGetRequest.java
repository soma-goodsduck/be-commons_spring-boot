package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryReportGetRequest {

    private String bcryptId;

    public CategoryReportGetRequest(String bcryptId) {
        this.bcryptId = bcryptId;
    }
}

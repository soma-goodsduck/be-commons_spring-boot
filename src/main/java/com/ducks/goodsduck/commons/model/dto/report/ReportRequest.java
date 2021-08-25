package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRequest {

    private String receiverBcryptId;
    private Long categoryReportId;
    private String content;

    public ReportRequest(String receiverBcryptId, Long categoryReportId, String content) {
        this.receiverBcryptId = receiverBcryptId;
        this.categoryReportId = categoryReportId;
        this.content = content;
    }
}

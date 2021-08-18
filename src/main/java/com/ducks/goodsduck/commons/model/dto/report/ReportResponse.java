package com.ducks.goodsduck.commons.model.dto.report;

import com.ducks.goodsduck.commons.model.entity.Report;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReportResponse {

    private String receiverName;
    private String categoryReportType;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isExist = false;

    public ReportResponse(Report report) {
        this.receiverName = report.getUser().getNickName();
        this.categoryReportType = report.getCategoryReport().getType();
        this.content = report.getContent();
        this.createdAt = report.getCreatedAt();
        this.isExist = true;
    }
}

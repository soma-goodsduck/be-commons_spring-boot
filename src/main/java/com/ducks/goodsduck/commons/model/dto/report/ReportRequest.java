package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
public class ReportRequest {

    private String receiverBcryptId;
    private Long categoryReportId;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));;

    public ReportRequest(String receiverBcryptId, Long categoryReportId, String content) {
        this.receiverBcryptId = receiverBcryptId;
        this.categoryReportId = categoryReportId;
        this.content = content;
    }
}

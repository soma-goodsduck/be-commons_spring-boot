package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRequest {

    private String receiverBcryptId;
    private Long reportCategoryId;
    private String content;
}

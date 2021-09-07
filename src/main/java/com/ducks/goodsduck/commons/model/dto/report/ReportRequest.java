package com.ducks.goodsduck.commons.model.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRequest {

    private String receiverBcryptId;
    private Long reportCategoryId;
    private String content;

    // ItemReport, ChatReport, UserReport, PostReport, CommentReport
    private String type;

    // Item, Post, User, Chat, Comment Id
    private String id;
}

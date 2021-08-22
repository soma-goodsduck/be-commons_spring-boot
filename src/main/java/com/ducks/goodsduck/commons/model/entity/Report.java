package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id @Column(name = "REPORT_ID")
    @GeneratedValue
    private Long id;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_REPORT_ID")
    private CategoryReport categoryReport;

    public Report(Long senderId, String content, LocalDateTime createdAt, User user, CategoryReport categoryReport) {
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.user = user;
        this.categoryReport = categoryReport;
    }

    public Report(ReportRequest reportRequest, CategoryReport categoryReport, User receiver, User user) {
        this.senderId = receiver.getId();
        this.content = reportRequest.getContent();
        this.createdAt = reportRequest.getCreatedAt();
        this.user = user;
        this.categoryReport = categoryReport;
    }
}

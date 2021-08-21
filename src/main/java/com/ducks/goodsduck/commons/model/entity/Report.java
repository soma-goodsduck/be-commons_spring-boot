package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public Report(Long senderId, String content, User user, CategoryReport categoryReport) {
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.user = user;
        this.categoryReport = categoryReport;
    }

    public Report(ReportRequest reportRequest, CategoryReport categoryReport, User receiver, User sender) {
        this.senderId = sender.getId();
        this.content = reportRequest.getContent();
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.user = receiver;
        this.categoryReport = categoryReport;
    }
}

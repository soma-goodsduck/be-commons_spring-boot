package com.ducks.goodsduck.commons.model.entity.report;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "report_type")
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;
    private Long senderId;
    private String senderNickName;
    private String content;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category reportCategory;

    public Report(ReportRequest reportRequest, Category category, User sender, User receiver) {
        this.senderId = sender.getId();
        this.senderNickName = sender.getNickName();
        this.receiver = receiver;
        this.content = reportRequest.getContent() == null ? category.getName() : reportRequest.getContent();
        this.reportCategory = category;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
    }
}

package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.report.CategoryReportDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryReport {

    @Id @Column(name = "CATEGORY_REPORT_ID")
    @GeneratedValue
    private Long id;

    private String type;

    public CategoryReport(String type) {
        this.type = type;
    }

    public CategoryReport(CategoryReportDto categoryReportRequest) {
        this.type = categoryReportRequest.getContent();
    }
}

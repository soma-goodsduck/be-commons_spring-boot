package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
}
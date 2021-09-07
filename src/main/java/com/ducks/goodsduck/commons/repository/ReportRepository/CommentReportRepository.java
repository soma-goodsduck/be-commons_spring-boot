package com.ducks.goodsduck.commons.repository.ReportRepository;

import com.ducks.goodsduck.commons.model.entity.report.CommentReport;
import com.ducks.goodsduck.commons.model.entity.report.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
}
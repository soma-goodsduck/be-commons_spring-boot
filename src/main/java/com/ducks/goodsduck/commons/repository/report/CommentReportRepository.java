package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
}
package com.ducks.goodsduck.commons.repository.ReportRepository;

import com.ducks.goodsduck.commons.model.entity.report.ItemReport;
import com.ducks.goodsduck.commons.model.entity.report.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
}
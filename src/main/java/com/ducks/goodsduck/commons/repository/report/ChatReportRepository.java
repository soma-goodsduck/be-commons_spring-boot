package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.ChatReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {
}

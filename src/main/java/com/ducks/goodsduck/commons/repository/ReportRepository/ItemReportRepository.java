package com.ducks.goodsduck.commons.repository.ReportRepository;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.report.ItemReport;
import com.ducks.goodsduck.commons.model.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {

    List<ItemReport> findByItemId(Long itemId);
}
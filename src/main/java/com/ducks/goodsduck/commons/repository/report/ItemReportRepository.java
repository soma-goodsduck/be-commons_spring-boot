package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.ItemReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {

    List<ItemReport> findByItemId(Long itemId);
}
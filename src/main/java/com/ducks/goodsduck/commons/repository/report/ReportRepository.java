package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.Report;
import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
//    Boolean existsByUserAndSenderId(User user, Long senderId);

    Boolean existsByReceiverAndSenderId(User receiver, Long senderId);
    List<Report> findByReceiver(User receiver);
}
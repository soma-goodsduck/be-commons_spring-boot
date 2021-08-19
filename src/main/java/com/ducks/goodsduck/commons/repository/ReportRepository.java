package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Report;
import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Boolean existsByUserAndSenderId(Long userId, Long senderId);
    List<Report> findByUser(User user);
}

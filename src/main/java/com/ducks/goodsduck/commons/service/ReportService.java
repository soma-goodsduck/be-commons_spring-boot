package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.report.CategoryReportDto;
import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.dto.report.ReportResponse;
import com.ducks.goodsduck.commons.model.entity.CategoryReport;
import com.ducks.goodsduck.commons.model.entity.Report;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.CategoryReportRepository;
import com.ducks.goodsduck.commons.repository.ReportRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.UserRole.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final CategoryReportRepository categoryReportRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public CategoryReportDto addCategoryReport(CategoryReportDto categoryReportRequest) {
        return new CategoryReportDto(categoryReportRepository.save(new CategoryReport(categoryReportRequest)));
    }

    public ReportResponse addReportFromUser(Long userId, ReportRequest reportRequest) {
        User sender = userRepository.findById(userId).orElseThrow(() -> {
            throw new NoResultException("User not founded.");
        });

        User receiver = userRepository.findByBcryptId(reportRequest.getReceiverBcryptId());

        CategoryReport categoryReport = categoryReportRepository.findById(reportRequest.getCategoryReportId())
                .orElseThrow(() -> {
                    throw new NoResultException("CategoryReport not founded.");
                });

        if (reportRepository.existsByUserAndSenderId(sender.getId(), receiver.getId())) {
            return new ReportResponse();
        }

        return new ReportResponse(
                reportRepository.save(new Report(reportRequest, categoryReport, receiver, sender))
        );
    }

    public List<ReportResponse> getReportsForUser(Long userId, Long receiverId) throws IllegalAccessException {
        User loginUser = userRepository.findById(userId).orElseThrow(() -> {
            throw new NoResultException("User not founded.");
        });

        User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
            throw new NoResultException("User not founded.");
        });

        if (!loginUser.getRole().equals(ADMIN)) {
            throw new IllegalAccessException("관리자 권한이 필요합니다.");
        }

        return reportRepository.findByUser(receiver)
                .stream()
                .map(report -> new ReportResponse(report))
                .collect(Collectors.toList());
    }
}

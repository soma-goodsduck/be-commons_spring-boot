package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryResponse;
import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryDto;
import com.ducks.goodsduck.commons.model.dto.report.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import com.ducks.goodsduck.commons.model.entity.category.CommentReportCategory;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.category.CategoryRepository;
import com.ducks.goodsduck.commons.repository.category.CommentReportCategoryRepository;
import com.ducks.goodsduck.commons.repository.category.ItemReportCategoryRepository;
import com.ducks.goodsduck.commons.repository.category.PostReportCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.UserRole.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;
    private final PostReportCategoryRepository postReportCategoryRepository;
    private final ItemReportCategoryRepository itemReportCategoryRepository;
    private final CommentReportCategoryRepository commentReportCategoryRepository;

//    public CategoryReportResponse getCategoryReportWithUserNickName(String bcryptId) {
//        User receiver = userRepository.findByBcryptId(bcryptId);
//        if (receiver == null) {
//            throw new NoResultException("User not founded.");
//        }
//        List<CategoryReportGetResponse> categoryReports = categoryReportRepository.findAll()
//                .stream()
//                .map(categoryReport -> new CategoryReportGetResponse(categoryReport))
//                .collect(Collectors.toList());
//        return new CategoryReportResponse(receiver.getNickName(), categoryReports);
//    }
//
//    public ReportResponse addReportFromUser(Long userId, ReportRequest reportRequest) {
//        User sender = userRepository.findById(userId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        User receiver = userRepository.findByBcryptId(reportRequest.getReceiverBcryptId());
//
//        CategoryReport categoryReport = categoryReportRepository.findById(reportRequest.getCategoryReportId())
//                .orElseThrow(() -> {
//                    throw new NoResultException("CategoryReport not founded.");
//                });
//
////        if (reportRepository.existsByUserAndSenderId(receiver, sender.getId())) {
////            return new ReportResponse();
////        }
//
//        return new ReportResponse();
//
////        return new ReportResponse(
////                reportRepository.save(new Report(reportRequest, categoryReport, receiver, sender))
////        );
//    }

    public ReportResponse addReportV2(Long userId, ReportRequest reportRequest) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> { throw new NoResultException("Not Find User in CategoryService.addReport");});

        User receiver = userRepository.findByBcryptId(reportRequest.getReceiverBcryptId());
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.addReport");
        }

        Category category = categoryRepository.findById(reportRequest.getReportCategoryId())
                .orElseThrow(() -> { throw new NoResultException("Not Find ReportCategory in CategoryService.addReport");});

        if(reportRepository.existsByReceiverAndSenderId(receiver, sender.getId())) {
            return new ReportResponse();
        }

        return new ReportResponse(reportRepository.save(new Report(reportRequest, category, receiver, sender)));
    }

    public ReportCategoryResponse getItemReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getItemReportCategory");
        }

        List<ReportCategoryDto> reportCategoryList = itemReportCategoryRepository.findAll()
                .stream()
                .map(postReportCategory -> new ReportCategoryDto(postReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getPostReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getPostReportCategory");
        }

        List<ReportCategoryDto> reportCategoryList = postReportCategoryRepository.findAll()
                .stream()
                .map(postReportCategory -> new ReportCategoryDto(postReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getCommentReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getPostReportCategory");
        }

        List<ReportCategoryDto> reportCategoryList = commentReportCategoryRepository.findAll()
                .stream()
                .map(postReportCategory -> new ReportCategoryDto(postReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }


    // TODO : 백오피스 ㄱㄱ
//    public List<ReportResponse> getReportsForUser(Long userId, Long receiverId) throws IllegalAccessException {
//        User loginUser = userRepository.findById(userId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        if (userId.equals(receiverId)) {
//            throw new IllegalArgumentException("신고하려는 대상이 본인입니다.");
//        }
//
//        if (!loginUser.getRole().equals(ADMIN)) {
//            throw new IllegalAccessException("관리자 권한이 필요합니다.");
//        }
//
//        return reportRepository.findByReceiver(receiver)
//                .stream()
//                .map(report -> new ReportResponse(report))
//                .collect(Collectors.toList());
//    }
//
//    public CategoryReportAddRequest addCategoryReport(Long userId, CategoryReportAddRequest categoryReportRequest) throws IllegalAccessException {
//        // HINT: 관리자가 아니면 예외 처리
//        User loginUser = userRepository.findById(userId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        if (!loginUser.getRole().equals(ADMIN)) throw new IllegalAccessException("관리자 권한이 필요합니다.");
//
//        return new CategoryReportAddRequest(categoryReportRepository.save(new CategoryReport(categoryReportRequest)));
//    }
}


package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryResponse;
import com.ducks.goodsduck.commons.model.dto.report.*;
import com.ducks.goodsduck.commons.service.ReportService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "신고 관련 APIs")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/v1/users/report")
    @ApiOperation("특정 사용자에 대한 신고 접수 V2")
    public ApiResult<ReportResponse> addReportV2(@RequestBody ReportRequest reportRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reportService.addReportV2(userId, reportRequest));
    }

    @GetMapping("/v1/items/report-category/{bcryptId}")
    @ApiOperation("아이템 게시글 신고 유형 보기 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getItemReportCategory(@PathVariable("bcryptId") String bcryptId, HttpServletRequest request) {
        return OK(reportService.getItemReportCategory(bcryptId));
    }

    @GetMapping("/v1/posts/report-category/{bcryptId}")
    @ApiOperation("커뮤니티 게시글 신고 유형 보기 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getPostReportCategory(@PathVariable("bcryptId") String bcryptId, HttpServletRequest request) {
        return OK(reportService.getPostReportCategory(bcryptId));
    }

    @GetMapping("/v1/comments/report-category/{bcryptId}")
    @ApiOperation("커뮤니티 게시글 신고 유형 보기 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getCommentReportCategory(@PathVariable("bcryptId") String bcryptId, HttpServletRequest request) {
        return OK(reportService.getCommentReportCategory(bcryptId));
    }

//    // TODO : 백오피스로 ㄱㄱ
//    @PostMapping("/v1/category-report")
//    @ApiOperation("(관리자) 신고 유형 추가")
//    public ApiResult<CategoryReportAddRequest> addCategoryReport(HttpServletRequest request, @RequestBody CategoryReportAddRequest categoryReportAddRequest) throws IllegalAccessException {
//        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
//        return OK(reportService.addCategoryReport(userId, categoryReportAddRequest));
//    }
//
//    // TODO : 백오피스로 ㄱㄱ
//    @GetMapping("/v1/users/{userId}/report")
//    @ApiOperation("(관리자) 특정 사용자가 받은 신고 목록 조회")
//    public ApiResult<List<ReportResponse>> addReport(@PathVariable("userId") Long receiverId, HttpServletRequest request) throws IllegalAccessException {
//        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
//        return OK(reportService.getReportsForUser(userId, receiverId));
//    }
}

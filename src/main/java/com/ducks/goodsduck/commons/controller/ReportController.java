package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.report.*;
import com.ducks.goodsduck.commons.service.ReportService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "신고 관련 APIs")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/v1/category-report")
    @ApiOperation("(관리자) 신고 유형 추가")
    public ApiResult<CategoryReportAddRequest> addCategoryReport(HttpServletRequest request, @RequestBody CategoryReportAddRequest categoryReportAddRequest) throws IllegalAccessException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reportService.addCategoryReport(userId, categoryReportAddRequest));
    }

    @GetMapping("/v1/category-report/users/{bcryptId}")
    @ApiOperation("신고 유형 보기 및 신고할 대상 닉네임 확인")
    public ApiResult<CategoryReportResponse> addCategoryReport(HttpServletRequest request, @PathVariable("bcryptId") String bcryptId) {
        return OK(reportService.getCategoryReportWithUserNickName(bcryptId));
    }

    @PostMapping("/v1/users/report")
    @ApiOperation("특정 사용자에 대한 신고 접수")
    public ApiResult<ReportResponse> addReport(HttpServletRequest request, @RequestBody ReportRequest reportRequest) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reportService.addReportFromUser(userId, reportRequest));
    }

    @GetMapping("/v1/users/{userId}/report")
    @ApiOperation("(관리자) 특정 사용자가 받은 신고 목록 조회")
    public ApiResult<List<ReportResponse>> addReport(HttpServletRequest request, @PathVariable("userId") Long receiverId) throws IllegalAccessException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reportService.getReportsForUser(userId, receiverId));
    }
}

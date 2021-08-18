package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.report.CategoryReportDto;
import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.dto.report.ReportResponse;
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

    @NoCheckJwt
    @PostMapping("/v1/category-report")
    @ApiOperation("신고 유형 추가")
    public ApiResult<CategoryReportDto> addCategoryReport(@RequestBody CategoryReportDto categoryReportDto) {
        return OK(reportService.addCategoryReport(categoryReportDto));
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

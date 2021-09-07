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
@Api(tags = "신고 APIs")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/v1/users/report")
    @ApiOperation("특정 사용자에 대한 신고 접수 (회원)")
    public ApiResult<ReportResponse> addReport(@RequestBody ReportRequest reportRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reportService.addReport(userId, reportRequest));
    }
}

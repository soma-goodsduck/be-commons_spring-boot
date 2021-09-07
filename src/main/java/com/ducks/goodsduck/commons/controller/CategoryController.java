package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryResponse;
import com.ducks.goodsduck.commons.repository.ReportRepository.ItemReportRepository;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.service.PostService;
import com.ducks.goodsduck.commons.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "카테고리 APIs")
public class CategoryController {

    private final ItemService itemService;
    private final PostService postService;
    private final ReportService reportService;

    @NoCheckJwt
    @ApiOperation(value = "아이템 카테고리 불러오기")
    @GetMapping("/v1/items/category")
    public ApiResult<List<CategoryResponse>> getItemCategory() {
        return OK(itemService.getItemCategory());
    }

    @NoCheckJwt
    @ApiOperation(value = "포스트 카테고리 불러오기")
    @GetMapping("/v1/posts/category")
    public ApiResult<List<CategoryResponse>> getPostCategory() {
        return OK(postService.getPostCategory());
    }

    @NoCheckJwt
    @GetMapping("/v1/items/report-category/{bcryptId}")
    @ApiOperation("아이템 게시글 신고 카테고리 보기 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getItemReportCategory(@PathVariable("bcryptId") String bcryptId) {
        return OK(reportService.getItemReportCategory(bcryptId));
    }

    @NoCheckJwt
    @GetMapping("/v1/posts/report-category/{bcryptId}")
    @ApiOperation("커뮤니티 게시글 신고 카테고리 보기 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getPostReportCategory(@PathVariable("bcryptId") String bcryptId) {
        return OK(reportService.getPostReportCategory(bcryptId));
    }

    @NoCheckJwt
    @GetMapping("/v1/comments/report-category/{bcryptId}")
    @ApiOperation("커뮤니티 게시글 신고 유형 카테고리 및 신고 대상 닉네임 확인")
    public ApiResult<ReportCategoryResponse> getCommentReportCategory(@PathVariable("bcryptId") String bcryptId) {
        return OK(reportService.getCommentReportCategory(bcryptId));
    }
}

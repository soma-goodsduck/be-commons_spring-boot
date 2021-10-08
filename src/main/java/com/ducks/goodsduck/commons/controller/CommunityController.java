package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.comment.MyComment;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.post.MyPost;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.service.CommunityService;
import com.ducks.goodsduck.commons.service.PostService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "커뮤니티 메뉴 관련 APIs")
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/v1/users/posts")
    @ApiOperation("내가 작성한 포스트 보기 (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getMyPost(@RequestParam("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getMyPost(userId, postId));
    }

    @GetMapping("/v1/users/comments")
    @ApiOperation("내가 작성한 댓글 보기 (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getMyComment(@RequestParam("commentId") Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getMyComment(userId, commentId));
    }

    @GetMapping("/v2/users/comments")
    @ApiOperation("내가 작성한 댓글 보기 V2 (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getMyCommentV2(@RequestParam("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getMyCommentV2(userId, postId));
    }

    @GetMapping("/v1/users/like-posts")
    @ApiOperation("내가 좋아요한 포스트 보기 (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getMyLikePost(@RequestParam("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getMyLikePost(userId, postId));
    }

    @GetMapping("/v1/community/free-market")
    @ApiOperation("무료나눔장터 포스트 목록 조회 + 좋아하는 아이돌 그룹 전체 필터링 API (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getFreeMarket(@RequestParam("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getFreePostList(userId, postId));
    }

    @GetMapping("/v1/community/free-market/filter")
    @ApiOperation("무료나눔장터 포스트 목록 조회 + 특정 아이돌 그룹 필터링 API (회원)")
    public ApiResult<HomeResponse<PostDetailResponse>> getFreeMarket(@RequestParam("postId") Long postId,
                                                                     @RequestParam("idolGroup") Long idolGroupId,
                                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(communityService.getFreePostListFilterByIdolGroup(userId, idolGroupId, postId));
    }
}

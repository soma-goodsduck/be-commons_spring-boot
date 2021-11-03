package com.ducks.goodsduck.commons.controller;

import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.service.PostService;
import com.ducks.goodsduck.commons.service.UserPostService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "커뮤니티 게시글 APIs")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final UserPostService userPostService;

    // TODO : gif 리사이즈...
    @ApiOperation("포스트 업로드 API")
    @PostMapping("/v1/posts")
    public ApiResult<Long> uploadPost(@RequestParam String stringPostDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                      HttpServletRequest request) throws IOException, MetadataException {

        PostUploadRequest postUploadRequest = new ObjectMapper().readValue(stringPostDto, PostUploadRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(postService.upload(postUploadRequest, multipartFiles, userId));
    }

    @ApiOperation("포스트 상세보기 API")
    @GetMapping("/v1/posts/{postId}")
    public ApiResult<PostDetailResponse> showPostDetail(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(postService.showDetailWithLike(userId, postId));
    }

    @ApiOperation("포스트 수정 API")
    @PutMapping("/v1/posts/{postId}")
    public ApiResult<Long> updatePost(@PathVariable("postId") Long postId,
                                      @RequestParam String stringPostDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                      HttpServletRequest request) throws IOException {

        PostUpdateRequest postUpdateRequest = new ObjectMapper().readValue(stringPostDto, PostUpdateRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(postService.edit(postId, postUpdateRequest, multipartFiles, userId));
    }

    @ApiOperation("포스트 삭제 API")
    @DeleteMapping("/v1/posts/{postId}")
    public ApiResult<Long> deletePost(@PathVariable("postId") Long postId) {
        return OK(postService.delete(postId));
    }

    @ApiOperation("포스트 삭제 V2 API")
    @DeleteMapping("/v2/posts/{postId}")
    public ApiResult<Long> deletePostV2(@PathVariable("postId") Long postId) {
        return OK(postService.deleteV2(postId));
    }

    @ApiOperation("특정 포스트 좋아요 요청 API")
    @PostMapping("/v1/posts/{postId}/like")
    public ApiResult<Boolean> likePost(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userPostService.likePost(userId, postId));
    }

    @ApiOperation("특정 포스트 좋아요 취소 API")
    @PostMapping("/v1/posts/{postId}/dislike")
    public ApiResult<Boolean> dislikePost(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userPostService.dislikePost(userId, postId));
    }

    @ApiOperation("포스트 목록 조회 + 좋아하는 아이돌 그룹 전체 필터링 API in 홈")
    @GetMapping("/v1/posts")
    public ApiResult<HomeResponse<PostDetailResponse>> getPostList(@RequestParam("postId") Long postId,
                                                                   HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(postService.getPostList(userId, postId));
    }

    @ApiOperation("포스트 목록 조회 + 특정 아이돌 그룹 필터링 API in 홈")
    @GetMapping("/v1/posts/filter")
    public ApiResult<HomeResponse<PostDetailResponse>> getPostListFilterByIdolGroup(@RequestParam("idolGroup") Long idolGroupId,
                                                                                    @RequestParam("postId") Long postId,
                                                                                    HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(postService.getPostListFilterByIdolGroup(userId, idolGroupId, postId));
    }

    // 특정 포스트 차단 API
    @PostMapping("/v1/posts/blocked-posts/{postId}")
    @ApiOperation(value = "특정 커뮤니티 게시글 차단 API", notes = "특정 포스트를 사용자의 차단 리스트에 추가함.")
    public ApiResult<Boolean> addBlockedUser(HttpServletRequest request, @PathVariable("postId") Long postId) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.addBlockedPost(userId, postId));
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.repository.category.PostCategoryRepository;
import com.ducks.goodsduck.commons.service.PostService;
import com.ducks.goodsduck.commons.service.UserPostService;
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

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "커뮤니티 게시글 APIs")
public class PostController {

    private final PostService postService;
    private final UserPostService userPostService;

    private final PostCategoryRepository postCategoryRepository;
    private final UserRepository userRepository;

    // TODO : gif 리사이즈...
    @ApiOperation("포스트 업로드 API")
    @PostMapping("/v1/posts")
    public ApiResult<Long> uploadPost(@RequestParam String stringPostDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                      HttpServletRequest request) throws JsonProcessingException {

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
    @Transactional
    public ApiResult<HomeResponse<PostDetailResponse>> getPosts(@RequestParam("postId") Long postId,
                                                                HttpServletRequest request) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext = false;
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPosts"));

        List<PostDetailResponse> postList = postService.getPosts(userId, postId);
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return OK(new HomeResponse(hasNext, new LoginUser(user), postList));
    }

    @ApiOperation("포스트 목록 조회 + 특정 아이돌 그룹 필터링 API in 홈")
    @GetMapping("/v1/posts/filter")
    @Transactional
    public ApiResult<HomeResponse<PostDetailResponse>> getPostsWithFilterIdolGroup(@RequestParam("idolGroup") Long idolGroupId,
                                                                                   @RequestParam("postId") Long postId,
                                                                                   HttpServletRequest request) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext = false;
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoResultException("Not find user in PostController.getPostsWithFilterIdolGroup"));

        List<PostDetailResponse> postList = postService.getPostsWithFilterIdolGroup(userId, idolGroupId, postId);
        if(postList.size() == pageableSize + 1) {
            hasNext = true;
            postList.remove(pageableSize);
        }

        return OK(new HomeResponse(hasNext, new LoginUser(user), postList));
    }

    @NoCheckJwt
    @ApiOperation(value = "포스트 카테고리 불러오기 in 포스트 등록")
    @GetMapping("/v1/posts/category")
    @javax.transaction.Transactional
    public ApiResult<List<CategoryResponse>> getItemCategory() {
        return OK(postCategoryRepository.findAll()
                .stream()
                .map(postCategory -> new CategoryResponse(postCategory))
                .collect(Collectors.toList()));
    }
}

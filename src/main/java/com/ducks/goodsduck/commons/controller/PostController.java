package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequestV2;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostDetailResponse;
import com.ducks.goodsduck.commons.model.dto.post.PostUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.post.PostUploadRequest;
import com.ducks.goodsduck.commons.service.PostService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Api(tags = "커뮤니티 APIs")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @NoCheckJwt
    @ApiOperation("포스트 업로드 API")
    @PostMapping("/v1/posts")
    public ApiResult<Long> uploadPost(@RequestParam String stringPostDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                      @RequestParam(value = "idolGroup") Long idolGroupId, HttpServletRequest request) throws JsonProcessingException {

        PostUploadRequest postUploadRequest = new ObjectMapper().readValue(stringPostDto, PostUploadRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        userId = 8L;
        return OK(postService.upload(postUploadRequest, multipartFiles, userId, idolGroupId));
    }

    @NoCheckJwt
    @ApiOperation("포스트 상세보기 API")
    @GetMapping("/v1/posts/{postId}")
    public ApiResult<PostDetailResponse> showPostDetail(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        userId = 8L;
        return OK(postService.showDetail(postId, userId));
    }

    @NoCheckJwt
    @ApiOperation("포스트 수정 API")
    @PutMapping("/v1/posts/{postId}")
    public ApiResult<PostDetailResponse> updatePost(@PathVariable("postId") Long postId,
                                                    @RequestParam String stringPostDto,
                                                    @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                                    HttpServletRequest request) throws JsonProcessingException {

        PostUpdateRequest postUpdateRequest = new ObjectMapper().readValue(stringPostDto, PostUpdateRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        userId = 8L;
//        return OK(postService.edit(postId, postUpdateRequest, multipartFiles, userId));
        postService.edit(postId, postUpdateRequest, multipartFiles, userId);

    }

    @NoCheckJwt
    @ApiOperation("포스트 삭제 API")
    @DeleteMapping("/v1/posts/{postId}")
    public ApiResult<PostDetailResponse> deletePost(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        userId = 8L;
        return OK(postService.delete(postId));
    }
}

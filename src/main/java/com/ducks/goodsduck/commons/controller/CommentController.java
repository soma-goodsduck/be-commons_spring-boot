package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.comment.*;
import com.ducks.goodsduck.commons.service.CommentService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "커뮤니티 댓글 CRUD APIs")
public class CommentController {

    private final CommentService commentService;

    @ApiOperation(value = "댓글 업로드 API")
    @PostMapping("/v1/comments")
    public ApiResult<Long> uploadComment(@RequestBody CommentUploadRequest commentUploadRequest,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.uploadComment(commentUploadRequest, userId));
    }

    @ApiOperation(value = "댓글 업로드 API V2")
    @PostMapping("/v2/comments")
    public ApiResult<Long> uploadCommentV2(@RequestBody CommentUploadRequest commentUploadRequest,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.uploadCommentV2(commentUploadRequest, userId));
    }

    @ApiOperation(value = "댓글 업로드 API V3")
    @PostMapping("/v3/comments")
    public ApiResult<Long> uploadCommentV3(@RequestBody CommentUploadRequestV2 commentUploadRequest,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.uploadCommentV3(commentUploadRequest, userId));
    }

    @ApiOperation(value = "댓글 수정 API")
    @PutMapping("/v1/comments/{commentId}")
    public ApiResult<Long> updateComment(@PathVariable("commentId") Long commentId,
                                         @RequestBody CommentUpdateRequest commentUpdateRequest) {
        return OK(commentService.updateComment(commentId, commentUpdateRequest));
    }

    @ApiOperation(value = "댓글 삭제 API")
    @DeleteMapping("/v1/comments/{commentId}")
    public ApiResult<Long> deleteComment(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.deleteComment(userId, commentId));
    }

    @ApiOperation(value = "댓글 삭제 API V2")
    @DeleteMapping("/v2/comments/{commentId}")
    public ApiResult<Long> deleteCommentV2(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.deleteCommentV2(userId, commentId));
    }

    @ApiOperation(value = "관련 포스트의 댓글 목록 조회 API")
    @GetMapping("/v1/comments/{postId}")
    public ApiResult<List<CommentDto>> showCommentsOfPost(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.getCommentsOfPost(userId, postId));
    }

    @ApiOperation(value = "관련 포스트의 댓글 목록 조회 API V2")
    @GetMapping("/v2/comments/{postId}")
    public ApiResult<List<CommentSimpleDto>> showCommentsOfPostV2(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.getCommentsOfPostV2(userId, postId));
    }

    @ApiOperation(value = "관련 포스트의 댓글 목록 조회 API V3")
    @GetMapping("/v3/comments/{postId}")
    public ApiResult<List<CommentDto>> showCommentsOfPostV3(@PathVariable("postId") Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.getCommentsOfPostV3(userId, postId));
    }

    @ApiOperation(value = "일반댓글 <-> 비밀댓글 변경 API")
    @GetMapping("/v1/comments/{commentId}/change-state")
    public ApiResult<Boolean> changeCommentState(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(commentService.changeCommentState(userId, commentId));
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.comment.CommentDto;
import com.ducks.goodsduck.commons.model.dto.comment.CommentSimpleDto;
import com.ducks.goodsduck.commons.model.dto.comment.CommentUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.comment.CommentUploadRequest;
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

    @ApiOperation(value = "댓글 수정 API")
    @PutMapping("/v1/comments/{commentId}")
    public ApiResult<Long> updateComment(@PathVariable("commentId") Long commentId,
                                         @RequestBody CommentUpdateRequest commentUpdateRequest) {
        return OK(commentService.updateComment(commentId, commentUpdateRequest));
    }

    @ApiOperation(value = "댓글 삭제 API")
    @DeleteMapping("/v1/comments/{commentId}")
    public ApiResult<Long> deleteComment(@PathVariable("commentId") Long commentId) {
        return OK(commentService.deleteComment(commentId));
    }

    @ApiOperation(value = "관련 포스트의 댓글 목록 조회 API")
    @GetMapping("/v1/comments/{postId}")
    public ApiResult<List<CommentDto>> showCommentsOfPost(@PathVariable("postId") Long postId) {
        return OK(commentService.getCommentsOfPost(postId));
    }

    @ApiOperation(value = "관련 포스트의 댓글 목록 조회 API (V2)")
    @GetMapping("/v2/comments/{postId}")
    public ApiResult<List<CommentSimpleDto>> showCommentsOfPostV2(@PathVariable("postId") Long postId) {
        return OK(commentService.getCommentsOfPostV2(postId));
    }
}

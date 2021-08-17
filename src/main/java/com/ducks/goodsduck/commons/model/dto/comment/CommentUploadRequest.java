package com.ducks.goodsduck.commons.model.dto.comment;

import lombok.Data;

@Data
public class CommentUploadRequest {

    private String content;
    private Long postId;
    private Long parentCommentId;
}

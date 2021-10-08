package com.ducks.goodsduck.commons.model.dto.comment;

import lombok.Data;

@Data
public class CommentUploadRequestV2 {

    private String content;
    private Long postId;
    // HINT: 일반 댓글일 시 null, 대댓글일 경우 대상 댓글 ID
    private Long receiveCommentId;
    private Boolean isSecret;
}

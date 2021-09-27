package com.ducks.goodsduck.commons.model.dto.comment;

import lombok.Data;

@Data
public class CommentUploadRequestV2 {

    private String content;
    private Long postId;
    private Long receiveCommentId;
    private Boolean isSecret;
}

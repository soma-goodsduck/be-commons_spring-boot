package com.ducks.goodsduck.commons.model.dto.comment;

import com.ducks.goodsduck.commons.model.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyComment {

    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private Long postId;
    private String idolGroupName;

    public MyComment(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.postId = comment.getPost().getId();
        this.idolGroupName = comment.getPost().getIdolGroup().getName();
    }
}

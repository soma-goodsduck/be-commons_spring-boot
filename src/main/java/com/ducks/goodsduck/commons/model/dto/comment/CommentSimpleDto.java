package com.ducks.goodsduck.commons.model.dto.comment;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentSimpleDto {

    private UserSimpleDto writer;
    private UserSimpleDto receiver;
    private Long commentId;
    private String content;
    private Integer level;
    private Boolean isSecret;
    private Boolean isPostOwnerComment;
    private Boolean isLoginUserComment;
    private LocalDateTime createdAt;

    public CommentSimpleDto(CommentDto commentDto) {
        this.commentId = commentDto.getCommentId();
        this.writer = commentDto.getWriter();
        this.receiver = commentDto.getReceiver();
        this.content = commentDto.getContent();
        this.level = commentDto.getLevel();
        this.isSecret = commentDto.getIsSecret();
        this.isLoginUserComment = commentDto.getIsLoginUserComment();
        this.isPostOwnerComment = commentDto.getIsPostOwnerComment();
        this.createdAt = commentDto.getCreatedAt();
    }
}

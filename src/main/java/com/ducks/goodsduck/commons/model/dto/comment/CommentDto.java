package com.ducks.goodsduck.commons.model.dto.comment;

import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDto {

    private Long commentId;
    private UserSimpleDto writer;
    private UserSimpleDto receiver;
    private String content;
    private Integer level;
    private Boolean isSecret;
    private Boolean isPostOwnerComment;
    private Boolean isLoginUserComment;
    private List<CommentDto> childComments = new ArrayList<>();

    public CommentDto(User user, Comment comment) {
        this.commentId = comment.getId();
        this.writer = new UserSimpleDto(user);
        this.content = comment.getContent();
        this.level = comment.getLevel();
        this.isSecret = comment.getIsSecret();
        this.isPostOwnerComment = false;
        this.isLoginUserComment = false;
    }
}

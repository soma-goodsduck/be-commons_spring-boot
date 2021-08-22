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

    private UserSimpleDto writer;
    private UserSimpleDto receiver;
    private String content;
    private Integer level;
    private Boolean isSecret;
    private List<CommentDto> childComments = new ArrayList<>();

    public CommentDto(User user, Comment comment) {
        this.writer = new UserSimpleDto(user);
        this.content = comment.getContent();
        this.level = comment.getLevel();
        this.isSecret = comment.getIsSecret();
    }
}

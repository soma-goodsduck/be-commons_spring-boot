package com.ducks.goodsduck.commons.model.dto.comment;

import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import lombok.Data;

@Data
public class CommentSimpleDto {

    private UserSimpleDto writer;
    private UserSimpleDto receiver;
    private String content;
    private Integer level;

    public CommentSimpleDto(CommentDto commentDto) {
        this.writer = commentDto.getWriter();
        this.receiver = commentDto.getReceiver();
        this.content = commentDto.getContent();
        this.level = commentDto.getLevel();
    }
}

package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewRequest {

    private String chatRoomId;
    private String content;
    private Integer score;
}

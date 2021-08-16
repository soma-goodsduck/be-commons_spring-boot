package com.ducks.goodsduck.commons.model.dto.review;

import com.ducks.goodsduck.commons.model.enums.NotificationType;
import com.ducks.goodsduck.commons.model.enums.ReviewType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewRequest {

    private Long itemId;
    private String chatRoomId;
    private String content;
    private Integer score;
    private NotificationType reviewType;
}

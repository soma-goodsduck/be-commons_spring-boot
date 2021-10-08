package com.ducks.goodsduck.commons.model.dto.review;

import com.ducks.goodsduck.commons.model.entity.Review;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReviewResponse {

    private String writerImageUrl;
    private String writerNickName;
    private String content;
    private Integer score;
    private LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.writerImageUrl = review.getUser().getImageUrl();
        this.writerNickName = review.getUser().getNickName();
        this.content = review.getContent();
        this.score = review.getScore();
        this.createdAt = review.getCreatedAt();
    }
}

package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.dto.review.ReviewResponse;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Review;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OtherUserPageDto {

    private UserSimpleDto user;
    private LocalDateTime lastLoginAt;
    private Integer itemCount;
    private Long reviewCount;
    private Integer stampCount;
    private List<ItemSummaryDto> items;
    private List<ReviewResponse> reviews;

    public OtherUserPageDto(User user, Integer itemCount, Long reviewCount, List<Item> items, List<Review> reviews) {
        this.user = new UserSimpleDto(user);
        this.lastLoginAt = user.getLastLoginAt();
        this.itemCount = itemCount;
        this.reviewCount = reviewCount;
        this.stampCount = 10;
        this.items = items.stream()
                .map(item -> new ItemSummaryDto(item))
                .collect(Collectors.toList());
        this.reviews = reviews.stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }
}

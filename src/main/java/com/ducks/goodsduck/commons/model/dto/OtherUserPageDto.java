package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.item.ItemSimpleDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Review;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OtherUserPageDto {

    private UserSimpleDto user;
    private Integer itemCount;
    private Long reviewCount;
    private Integer stampCount;
    private List<ItemSimpleDto> items = new ArrayList<>();
    private List<ReviewResponse> reviews = new ArrayList<>();

    public OtherUserPageDto(User user, Integer itemCount, Long reviewCount, List<Item> items, List<Review> reviews) {
        this.user = new UserSimpleDto(user);
        this.itemCount = itemCount;
        this.reviewCount = reviewCount;
        this.stampCount = 10;
        this.items = items.stream()
                .map(item -> new ItemSimpleDto(item))
                .collect(Collectors.toList());
        this.reviews = reviews.stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }
}

package com.ducks.goodsduck.commons.model.dto.post;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {

    private String content;
    private Long postCategoryId;
    private List<String> imageUrls;
}

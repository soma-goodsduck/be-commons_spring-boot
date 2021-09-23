package com.ducks.goodsduck.commons.model.dto.post;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {

    private String content;
    private List<String> imageUrls;
    private String postCategory;
}

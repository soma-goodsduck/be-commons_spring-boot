package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.category.PostCategory;
import lombok.Data;

@Data
public class PostCategoryDto {

    Long postCategoryId;
    String postCategoryName;

    public PostCategoryDto(PostCategory postCategory) {
        this.postCategoryId = postCategory.getId();
        this.postCategoryName = postCategory.getName();
    }
}

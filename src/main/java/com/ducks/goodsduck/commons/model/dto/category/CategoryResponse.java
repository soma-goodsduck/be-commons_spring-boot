package com.ducks.goodsduck.commons.model.dto.category;

import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.Data;

@Data
public class CategoryResponse {

    private Long categoryId;
    private String categoryName;

    public CategoryResponse(Category category) {
        this.categoryId = category.getId();
        this.categoryName = category.getName();
    }
}

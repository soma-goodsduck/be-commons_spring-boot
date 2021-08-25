package com.ducks.goodsduck.commons.model.dto.category;

import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.Data;

@Data
public class CategoryResponse {

    // TODO : itemCategoryId, itemCategoryName;
    private Long CategoryItemId;
    private String CategoryItemName;

    public CategoryResponse(Category category) {
        this.CategoryItemId = category.getId();
        this.CategoryItemName = category.getName();
    }
}

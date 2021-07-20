package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.CategoryItem;
import lombok.Data;

@Data
public class CategoryItemDto {

    private Long CategoryItemId;
    private String CategoryItemName;

    public CategoryItemDto(CategoryItem categoryItem) {
        this.CategoryItemId = categoryItem.getId();
        this.CategoryItemName = categoryItem.getName();
    }
}

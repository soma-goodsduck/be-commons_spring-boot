package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.Category;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;

@Data
public class ItemDetailResponseCategory {

    private String name;

    public ItemDetailResponseCategory(Category category) {
        this.name = category.getName();
    }
}

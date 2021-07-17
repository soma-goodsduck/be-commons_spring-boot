package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.CategoryItem;
import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;

@Data
public class ItemDetailResponseCategory {

    private String name;

    public ItemDetailResponseCategory(CategoryItem categoryItem) {
        this.name = categoryItem.getName();
    }
}

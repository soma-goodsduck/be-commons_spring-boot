package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import lombok.Data;

@Data
public class ItemDetailResponseCategory {

    private String name;

    public ItemDetailResponseCategory(ItemCategory itemCategory) {
        this.name = itemCategory.getName();
    }
}

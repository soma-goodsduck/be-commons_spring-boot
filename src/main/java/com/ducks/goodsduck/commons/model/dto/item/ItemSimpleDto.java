package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemSimpleDto {

    private Long id;
    private String name;

    public ItemSimpleDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
    }
}
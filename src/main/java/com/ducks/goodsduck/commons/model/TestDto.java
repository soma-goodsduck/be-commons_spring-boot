package com.ducks.goodsduck.commons.model;

import com.ducks.goodsduck.commons.model.entity.Item;
import lombok.Data;

@Data
public class TestDto {

    private String name;

    public TestDto(Item item) {
        this.name = item.getName();
    }
}

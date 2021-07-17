package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.Image;
import lombok.Data;

@Data
public class ItemDetailResponseImage {

    private String url;

    public ItemDetailResponseImage(Image image) {
        this.url = image.getUrl();
    }
}

package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.Image.Image;
import lombok.Data;

@Data
public class ItemDetailResponseImage {

    private String url;
    private Boolean isBright;

    public ItemDetailResponseImage(Image image) {
        this.url = image.getUrl();
        this.isBright = image.getIsBright();
    }
}

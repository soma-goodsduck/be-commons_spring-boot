package com.ducks.goodsduck.commons.model.dto.post;

import com.ducks.goodsduck.commons.model.entity.Image.PostImage;
import lombok.Data;

@Data
public class PostDetailResponseImage {

    private String url;

    public PostDetailResponseImage(PostImage image) {
        this.url = image.getUrl();
    }
}

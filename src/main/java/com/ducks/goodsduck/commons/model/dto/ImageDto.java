package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.Image;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImageDto {

    private String originName;
    private String uploadName;
    private String url;

    public ImageDto(Image image) {
        this.originName = image.getOriginName();
        this.uploadName = image.getUploadName();
        this.url = image.getUrl();
    }

    public ImageDto(String originName, String uploadName) {
        this.originName = originName;
        this.uploadName = uploadName;
    }

    public ImageDto(String originName, String uploadName, String url) {
        this.originName = originName;
        this.uploadName = uploadName;
        this.url = url;
    }
}

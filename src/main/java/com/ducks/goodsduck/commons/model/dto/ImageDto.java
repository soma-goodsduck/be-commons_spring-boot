package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

@Data
public class ImageDto {

    private String originName;
    private String uploadName;
    private String url;

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

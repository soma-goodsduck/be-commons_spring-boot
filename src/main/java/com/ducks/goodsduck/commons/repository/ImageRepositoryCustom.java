package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Image;
import com.ducks.goodsduck.commons.model.entity.ItemImage;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepositoryCustom {

    List<Image> findByImageUrls(List<String> imageUrls);
    List<Image> findItemImages();
//    List<Image> findByImageType(ImageType imageType);
}

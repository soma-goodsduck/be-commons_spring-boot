package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Image;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepositoryCustom {

    List<Image> findByImageUrls(List<String> imageUrls);
}

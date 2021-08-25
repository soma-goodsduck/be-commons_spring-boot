package com.ducks.goodsduck.commons.repository.image;

import com.ducks.goodsduck.commons.model.entity.Image.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}

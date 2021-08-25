package com.ducks.goodsduck.commons.repository.image;

import com.ducks.goodsduck.commons.model.entity.Image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByUrl(String imageUrl);

//    List<Image> findAllByItemId(Long itemId);
}

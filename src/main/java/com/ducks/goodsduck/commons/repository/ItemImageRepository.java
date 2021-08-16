package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
}

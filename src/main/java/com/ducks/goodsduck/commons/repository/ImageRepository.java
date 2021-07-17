package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}

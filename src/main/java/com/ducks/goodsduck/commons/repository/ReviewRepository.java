package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
}

package com.ducks.goodsduck.commons.repository.review;

import com.ducks.goodsduck.commons.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByItemIdAndUserId(Long itemId, Long userId);
}

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Review;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepositoryCustom {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    Long countBySenderId(Long senderId);
    List<Review> findByUserId(Long userId);
    List<Review> findByReveiverId(Long receiverId);
    Long countByReveiverId(Long receiverId);
}
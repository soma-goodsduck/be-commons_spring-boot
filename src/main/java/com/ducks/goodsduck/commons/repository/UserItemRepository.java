package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    Long countByUserId(Long userId);
}

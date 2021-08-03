package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepositoryCustom {
    boolean existsByItemIdAndUserId(Long itemId, Long senderId);
    List<Tuple> findInItems(List<Item> items);
}

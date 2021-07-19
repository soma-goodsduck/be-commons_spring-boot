package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.User;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
}

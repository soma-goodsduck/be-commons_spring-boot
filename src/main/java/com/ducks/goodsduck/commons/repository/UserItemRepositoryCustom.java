package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.UserItem;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepositoryCustom {
    List<UserItem> findByUserIdAndItemId(Long userId, Long itemid);
    List<Tuple> findByUserId(Long userId);
    List<Item> findByUserIdV2(Long userId);
    Tuple findTupleByUserIdAndItemId(Long userId, Long itemId);

    List<UserItem> findByItemId(Long itemId);
}

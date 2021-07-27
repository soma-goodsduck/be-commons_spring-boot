package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
    List<Item> findAll(Pageable pageable);
    List<Tuple> findAllWithUserItemIdolGroup(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable);
    List<Tuple> findAllWithUserItemIdolGroupV2(Long userId, List<UserIdolGroup> userIdolGroups, String keyword, Pageable pageable);
    Tuple findByIdWithUserItem(Long userId, Long itemId);
    List<Tuple> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status);
    long updateTradeStatus(Long itemId, TradeStatus status);
}

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
    List<Tuple> findAllWithUserItem(Long userId);
    List<Tuple> findAllWithUserItem(Long userId, Pageable pageable);
    List<Tuple> findAllWithUserItemIdolGroup(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable);
    Tuple findByIdWithUserItem(Long userId, Long itemId);
    List<Tuple> findAllByUserIdAndTradeStatus(Long userId, List<TradeStatus> status);
    long updateTradeStatus(Long itemId, TradeStatus status);
}

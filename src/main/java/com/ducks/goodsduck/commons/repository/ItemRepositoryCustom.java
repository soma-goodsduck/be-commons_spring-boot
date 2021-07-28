package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
    Tuple findByIdWithUserItem(Long userId, Long itemId);
    List<Tuple> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status);
    long updateTradeStatus(Long itemId, TradeStatus status);

    // 홈
    List<Item> findAll(Pageable pageable);
    List<Item> findAllByIdolGroup(Long idolGroupId, Pageable pageable);
    List<Item> findAllByFilter(ItemFilterDto itemFilterDto, Pageable pageable);

    List<Tuple> findAllByUserIdolGroupsWithUserItem(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable);
    List<Tuple> findAllByIdolGroupWithUserItem(Long userId, Long idolGroupId, Pageable pageable);
    List<Tuple> findAllByFilter(Long userId, ItemFilterDto itemFilterDto, Pageable pageable);
}

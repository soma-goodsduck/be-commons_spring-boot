package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
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
    Tuple findByIdWithUserItem(Long userId, Long itemId);
    List<Tuple> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status);
    long updateTradeStatus(Long itemId, TradeStatus status);

    // 비회원 홈
    List<Item> findAll(Pageable pageable);
    List<Tuple> findAllV2(Pageable pageable, String keyword);
    List<Item> findAllV3(Long userItem);

    // 비회원 홈 (아이돌필터링)
    List<Item> findAllByIdolGroup(Long idolGroupId, Pageable pageable);
    List<Tuple> findAllByIdolGroupV2(Long idolGroupId, Pageable pageable, String keyword);
    List<Item> findAllByIdolGroupV3(Long idolGroupId, Long itemId);

    // 비회원 홈 (전체필터링)
    List<Item> findAllByFilterWithUserItem(ItemFilterDto itemFilterDto, Pageable pageable);
    List<Tuple> findAllByFilterWithUserItemV2(ItemFilterDto itemFilterDto, Pageable pageable, String keyword);
    List<Item> findAllByFilterWithUserItemV3(ItemFilterDto itemFilterDto, Long itemId);

    // 회원 홈
    List<Tuple> findAllByUserIdolGroupsWithUserItem(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable);
    List<Tuple> findAllByUserIdolGroupsWithUserItemV2(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable, String keyword);
    List<Tuple> findAllByUserIdolGroupsWithUserItemV3(Long userId, List<UserIdolGroup> userIdolGroups, Long itemId);

    // 회원 홈 (아이돌필터링)
    List<Tuple> findAllByIdolGroupWithUserItem(Long userId, Long idolGroupId, Pageable pageable);
    List<Tuple> findAllByIdolGroupWithUserItemV2(Long userId, Long idolGroupId, Pageable pageable, String keyword);
    List<Tuple> findAllByIdolGroupWithUserItemV3(Long userId, Long idolGroupId, Long itemId);

    // 회원 홈 (전체필터링)
    List<Tuple> findAllByFilterWithUserItem(Long userId, ItemFilterDto itemFilterDto, Pageable pageable);
    List<Tuple> findAllByFilterWithUserItemV2(Long userId, ItemFilterDto itemFilterDto, Pageable pageable, String keyword);
    List<Tuple> findAllByFilterWithUserItemV3(Long userId, ItemFilterDto itemFilterDto, Long itemId);
}

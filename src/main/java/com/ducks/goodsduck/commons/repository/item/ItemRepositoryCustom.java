package com.ducks.goodsduck.commons.repository.item;

import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.model.enums.Order;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
    long updateTradeStatus(Long itemId, TradeStatus status);


    // 마이페이지 & 다른유저페이지 거래내역
    List<Item> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status);
    List<Long> findAllByUserIdAndNotCompleted(Long userId);
    
    // 좋아요 확인
    Tuple findByIdWithUserItem(Long userId, Long itemId);

    // 비회원 홈
    List<Item> findAll(Pageable pageable);
    List<Tuple> findAllV2(Pageable pageable, String keyword);
    List<Item> findAllV3(Long userItem);

    // 비회원 홈 (아이돌필터링)
    List<Item> findAllByIdolGroup(Long idolGroupId, Pageable pageable);
    List<Tuple> findAllByIdolGroupV2(Long idolGroupId, Pageable pageable, String keyword);
    List<Item> findAllByIdolGroupV3(Long idolGroupId, Long itemId);

    // 비회원 홈 (전체필터링)
    List<Item> findAllByFilter(ItemFilterDto itemFilterDto, Pageable pageable);
    List<Tuple> findAllByFilterV2(ItemFilterDto itemFilterDto, Pageable pageable, String keyword);
    List<Item> findAllByFilterV3(ItemFilterDto itemFilterDto, Long itemId);

    // 회원 홈
    List<Tuple> findAllByUserIdolGroupsWithUserItem(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable);
    List<Tuple> findAllByUserIdolGroupsWithUserItemV2(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable, String keyword);
    List<Tuple> findAllByUserIdolGroupsWithUserItemV3(Long userId, List<UserIdolGroup> userIdolGroups, Long itemId);
    List<Tuple> findAllByUserIdolGroupsWithUserItemV4(Long userId, List<UserIdolGroup> userIdolGroups, Long itemId);  // 차단한 사용자 반영

    // 회원 홈 (아이돌필터링)
    List<Tuple> findAllByIdolGroupWithUserItem(Long userId, Long idolGroupId, Pageable pageable);
    List<Tuple> findAllByIdolGroupWithUserItemV2(Long userId, Long idolGroupId, Pageable pageable, String keyword);
    List<Tuple> findAllByIdolGroupWithUserItemV3(Long userId, Long idolGroupId, Long itemId);
    List<Tuple> findAllByIdolGroupWithUserItemV4(Long userId, Long idolGroupId, Long itemId);   // 차단한 사용자 반영

    // 회원 홈 (전체필터링)
    List<Tuple> findAllByFilter(Long userId, ItemFilterDto itemFilterDto, Pageable pageable);
    List<Tuple> findAllByFilterV2(Long userId, ItemFilterDto itemFilterDto, Pageable pageable, String keyword);
    List<Tuple> findAllByFilterV3(Long userId, ItemFilterDto itemFilterDto, Long itemId);
    List<Tuple> findAllByFilterV4(Long userId, ItemFilterDto itemFilterDto, Long itemId);   // 차단한 사용자 반영

    Tuple findItemAndUserByItemId(Long itemId);

    // 비회원 - 검색 및 itemId 기반 Limit
    List<Item> findByKeywordWithLimit(String keyword, Long itemId, Long price, Order order, Boolean complete);

    // 회원 - 검색 및 itemId 기반 Limit
    List<Tuple> findByKeywordWithUserItemAndLimit(Long userId, String keyword, Long itemId, Long price, Order order, Boolean complete);
}

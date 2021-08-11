package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceProposeRepositoryCustom {
    PricePropose findByUserIdAndItemId(Long userId, Long itemId);
    List<PricePropose> findAllByItemId(Long itemId);

    List<Tuple> findByItems(List<Item> items);
    List<Tuple> findByItemId(Long itemId);
    List<Tuple> findByUserId(Long userId);
    Long updatePrice(Long userId, Long priceProposeId, int price);
    Long updateStatus(Long priceProposeId, PriceProposeStatus status);
    Long countSuggestedInItems(List<Item> itemsByUserId);

    List<PricePropose> findAllByItemIdWithAllStatus(Long itemId);
}

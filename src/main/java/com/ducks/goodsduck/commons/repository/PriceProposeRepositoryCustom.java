package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceProposeRepositoryCustom {
    List<PricePropose> findByUserIdAndItemId(Long userId, Long itemId);
    List<PricePropose> findByItems(List<Item> items);
    List<Tuple> findByItemId(Long itemId);
    long updatePrice(Long userId, Long priceProposeId, int price);
    long updateStatus(Long priceProposeId, PriceProposeStatus status);
}

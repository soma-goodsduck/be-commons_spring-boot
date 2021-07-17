package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.PricePropose;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceProposeRepositoryCustom {
    List<PricePropose> findByUserIdAndItemId(Long userId, Long itemId);
    long updatePrice(Long userId, Long priceProposeId, int price);
}

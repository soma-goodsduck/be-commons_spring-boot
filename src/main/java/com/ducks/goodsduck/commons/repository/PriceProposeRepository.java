package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.PricePropose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceProposeRepository extends JpaRepository<PricePropose, Long> {
    List<PricePropose> findByItemId(Long itemId);
    List<PricePropose> findByUserId(Long userId);
}

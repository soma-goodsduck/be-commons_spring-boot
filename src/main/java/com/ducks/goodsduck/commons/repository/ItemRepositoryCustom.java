package com.ducks.goodsduck.commons.repository;

import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepositoryCustom {
    Tuple findByItemId(Long itemId);
    Page<Tuple> findAllWithUserItem(Long userId, Pageable pageable);
    Tuple findByIdWithUserItem(Long userId, Long itemId);
}

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}

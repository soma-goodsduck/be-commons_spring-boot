package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.CategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryItemRepository extends JpaRepository<CategoryItem, Long> {

    CategoryItem findByName(String categoryName);
}

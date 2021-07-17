package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByName(String name);
}

package com.ducks.goodsduck.commons.repository.category;

import com.ducks.goodsduck.commons.model.entity.category.CommunityCategory;
import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCategoryRepository extends JpaRepository<CommunityCategory, Long> {
}

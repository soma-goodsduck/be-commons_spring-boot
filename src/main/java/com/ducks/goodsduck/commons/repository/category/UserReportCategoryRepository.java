package com.ducks.goodsduck.commons.repository.category;

import com.ducks.goodsduck.commons.model.entity.category.UserReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportCategoryRepository extends JpaRepository<UserReportCategory, Long> {
}

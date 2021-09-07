package com.ducks.goodsduck.commons.repository.category;

import com.ducks.goodsduck.commons.model.entity.category.ChatReportCategory;
import com.ducks.goodsduck.commons.model.entity.category.CommentReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatReportCategoryRepository extends JpaRepository<ChatReportCategory, Long> {
}

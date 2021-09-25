package com.ducks.goodsduck.commons.repository.useritem;

import com.ducks.goodsduck.commons.model.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    @Query("select count(ui) from UserItem ui where ui.user.id = :userId and ui.item.deletedAt is null")
    Long countByUserId(@Param("userId") Long userId);

    List<UserItem> findAllByUserId(Long userId);
}

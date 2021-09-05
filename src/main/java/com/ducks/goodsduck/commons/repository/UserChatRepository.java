package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    List<UserChat> findByUserId(Long userId);
}

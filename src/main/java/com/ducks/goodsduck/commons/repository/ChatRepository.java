package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
}

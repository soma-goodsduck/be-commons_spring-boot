package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserChat;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatRepositoryCustom {

    List<UserChat> findAllByChatId(String chatId);
}

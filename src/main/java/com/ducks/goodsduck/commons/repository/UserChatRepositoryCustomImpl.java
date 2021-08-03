package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class UserChatRepositoryCustomImpl implements UserChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QItem item = QItem.item;
    private QChat chat = QChat.chat;
    private QUserChat userChat = QUserChat.userChat;

    public UserChatRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<UserChat> findAllByChatId(String chatId) {
        return queryFactory
                .select(userChat)
                .from(userChat)
                .where(userChat.chat.id.eq(chatId))
                .fetch();
    }

    @Override
    public List<Tuple> findChatAndItemByUserId(Long userId) {
        return queryFactory
                .select(chat, item)
                .from(userChat)
                .join(userChat.chat, chat)
                .join(userChat.item, item)
                .where(userChat.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Chat findByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory
                .select(chat)
                .from(userChat)
                .join(userChat.chat, chat)
                .where(userChat.user.id.eq(userId)
                        .and(userChat.item.id.eq(itemId)))
                .fetchOne();
    }
}

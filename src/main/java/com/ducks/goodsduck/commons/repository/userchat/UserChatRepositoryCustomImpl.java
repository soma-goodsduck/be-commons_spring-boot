package com.ducks.goodsduck.commons.repository.userchat;

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
    private QUser user = QUser.user;
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
    public User findSenderByChatIdAndUserId(String chatId, Long senderId) {
        return queryFactory
                .select(userChat.user)
                .from(userChat)
                .where(userChat.chat.id.eq(chatId)
                        .and(userChat.user.id.eq(senderId)))
                .fetchOne();
    }
    
    @Override
    public Chat findByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory
                .select(chat)
                .from(userChat)
                .join(userChat.chat, chat)
                .where(userChat.user.id.eq(userId)
                        .and(userChat.item.id.eq(itemId))
                        .and(chat.deletedAt.isNull()))
                .fetchOne();
    }

    @Override
    public List<Chat> findByUserIdAndItemIdWithDeleted(Long userId, Long itemId) {
        return queryFactory
                .select(chat)
                .from(userChat)
                .join(userChat.chat, chat)
                .where(userChat.user.id.eq(userId)
                        .and(userChat.item.id.eq(itemId)))
                .fetch();
    }

    @Override
    public List<Tuple> findByItemIdExceptItemOwner(Long itemOwnerId, Long itemId) {
        return queryFactory
                .select(userChat, user)
                .from(userChat)
                .join(user).on(userChat.user.eq(user))
                .where(userChat.user.id.ne(itemOwnerId)
                        .and(userChat.item.id.eq(itemId)))
                .orderBy(userChat.id.desc())
                .fetch();
    }

    @Override
    public List<UserChat> findByItemId(Long itemId) {
        return queryFactory
                .select(userChat)
                .from(userChat)
                .where(userChat.item.id.eq(itemId))
                .fetch();
    }

    @Override
    public UserChat findBySenderIdAndChatRoomId(Long senderId, String chatRoomId) {
        return queryFactory
                .select(userChat)
                .from(userChat)
                .where(userChat.user.id.ne(senderId)
                        .and(userChat.chat.id.eq(chatRoomId)))
                .fetchOne();
    }
}

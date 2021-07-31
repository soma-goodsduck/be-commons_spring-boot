package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class UserChatRepositoryCustomImpl implements UserChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QUser user = QUser.user;
    private QItem item = QItem.item;
    private QUserItem userItem = QUserItem.userItem;
    private QCategoryItem categoryItem = QCategoryItem.categoryItem;
    private QIdolMember idolMember = QIdolMember.idolMember;
    private QIdolGroup idolGroup = QIdolGroup.idolGroup;
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
}

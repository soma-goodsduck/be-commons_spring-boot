package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class UserItemRepositoryCustomImpl implements UserItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QUser user = QUser.user;
    private QItem item = QItem.item;
    private QUserItem userItem = QUserItem.userItem;
    private QCategoryItem categoryItem = QCategoryItem.categoryItem;
    private QIdolMember idolMember = QIdolMember.idolMember;
    private QIdolGroup idolGroup = QIdolGroup.idolGroup;

    public UserItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<UserItem> findByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory.selectFrom(userItem)
                .where(userItem.user.id.eq(userId).and(userItem.item.id.eq(itemId)))
                .fetch();
    }

    @Override
    public List<Tuple> findTupleByUserId(Long userId) {
        return queryFactory.select(userItem, item, categoryItem, idolMember, idolGroup)
                .from(userItem)
                .join(userItem.item, item)
                .join(item.categoryItem, categoryItem)
                .join(item.idolMember, idolMember)
                .join(idolMember.idolGroup, idolGroup)
                .join(userItem.user, user)
                .where(userItem.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Tuple findTupleByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory.select(userItem, item)
                .from(userItem)
                .join(userItem.item, item)
                .where(userItem.user.id.eq(userId).and(
                        item.id.eq(itemId)
                )).fetchOne();
    }
}

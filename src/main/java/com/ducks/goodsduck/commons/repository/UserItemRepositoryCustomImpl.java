package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.QImage;
import com.ducks.goodsduck.commons.model.entity.category.QItemCategory;
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
    private QImage image = QImage.image;
    private QUserItem userItem = QUserItem.userItem;
    private QItemCategory itemCategory = QItemCategory.itemCategory;
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
    public List<Tuple> findByUserId(Long userId) {
        return queryFactory.select(userItem, item, itemCategory, idolMember, idolGroup)
                .from(userItem)
                .join(userItem.item, item)
                .join(item.itemCategory, itemCategory)
                .join(item.idolMember, idolMember)
                .join(idolMember.idolGroup, idolGroup)
                .join(userItem.user, user)
                .where(userItem.user.id.eq(userId))
                .orderBy(userItem.id.desc())
                .fetch();
    }

    @Override
    public List<Item> findByUserIdV2(Long userId) {
        return queryFactory
                .select(userItem.item)
                .from(userItem)
                .where(userItem.user.id.eq(userId))
                .orderBy(userItem.item.id.desc())
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

    @Override
    public List<UserItem> findByItemId(Long itemId) {
        return queryFactory
                .select(userItem)
                .from(userItem)
                .where(userItem.item.id.eq(itemId))
                .fetch();
    }
}

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QUserItem userItem = QUserItem.userItem;
    private QItem item = QItem.item;
    private QUser user = QUser.user;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Tuple findByItemId(Long itemId) {

        return queryFactory.select(user, item)
                .from(item)
                .join(item.user, user)
                .where(item.id.eq(itemId))
                .fetchOne();
    }

    @Override
    public List<Tuple> findAllWithUserItem(Long userId, Pageable pageable) {

        return queryFactory.select(item, new CaseBuilder()
                                            .when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum())
                .from(item)
                .groupBy(item)
                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
                .fetch();

    }

    @Override
    public Tuple findByIdWithUserItem(Long userId, Long itemId) {
        return queryFactory.select(item, new CaseBuilder().when(userItem.user.id.eq(userId)).then(1L).otherwise(0L))
                .from(item)
                .join(userItem).on(userItem.item.eq(item))
                .where(item.id.eq(itemId))
                .fetchOne();

    }
}

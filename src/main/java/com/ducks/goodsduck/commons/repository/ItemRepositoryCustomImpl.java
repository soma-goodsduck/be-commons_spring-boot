package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<Tuple> findAllWithUserItem(Long userId, Pageable pageable) {

        JPAQuery<Tuple> query = queryFactory.select(item, new CaseBuilder()
                .when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum())
                .from(item)
                .groupBy(item)
                .leftJoin(userItem).on(userItem.item.id.eq(item.id));

        Sort sort = pageable.getSort();

        sort.stream().forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            PathBuilder orderByExpression = new PathBuilder(Item.class, "item");
            query.orderBy(new OrderSpecifier(direction, orderByExpression.get(property)));
        });

        long count = query.fetchCount();

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<Tuple> resultListOfTuple = query.fetch();

        Page<Tuple> pages = new PageImpl<>(resultListOfTuple, pageable, count);

        return pages;
    }

    @Override
    public Tuple findByIdWithUserItem(Long userId, Long itemId) {
        return queryFactory.select(item, new CaseBuilder().when(userItem.user.id.eq(userId)).then(1L).otherwise(0L))
                .from(item)
                .leftJoin(userItem).on(userItem.item.eq(item))
                .where(item.id.eq(itemId))
                .fetchOne();

    }
}

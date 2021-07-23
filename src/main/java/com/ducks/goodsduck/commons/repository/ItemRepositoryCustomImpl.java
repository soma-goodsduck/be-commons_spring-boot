package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.alias.Alias;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Slf4j
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QUserItem userItem = QUserItem.userItem;
    private QItem item = QItem.item;
    private QUser user = QUser.user;
    private QIdolMember idolMember = QIdolMember.idolMember;
    private QIdolGroup idolGroup = QIdolGroup.idolGroup;
    private QCategoryItem categoryItem = QCategoryItem.categoryItem;
    private QImage image = QImage.image;

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

//    @Override
//    public Page<Tuple> findAllWithUserItem(Long userId, Pageable pageable, Integer pageNumber) {
//
//        JPAQuery<Tuple> query = queryFactory.select(item, new CaseBuilder()
//                .when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum())
//                .from(item)
//                .groupBy(item)
//                .leftJoin(userItem).on(userItem.item.id.eq(item.id));
//
//        Sort sort = pageable.getSort();
//
//        sort.stream().forEach(order -> {
//            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
//            String property = order.getProperty();
//
//            PathBuilder orderByExpression = new PathBuilder(Item.class, "item");
//            query.orderBy(new OrderSpecifier(direction, orderByExpression.get(property)));
//        });
//
//        long count = query.fetchCount();
//
//        query.offset(pageNumber);
//        query.
//        query.limit(pageable.getPageSize());
//
//        List<Tuple> resultListOfTuple = query.fetch();
//
//        Page<Tuple> pages = new PageImpl<>(resultListOfTuple, pageable, count);
//
//        return pages;
//    }


    @Override
    public List<Tuple> findAllWithUserItem(Long userId, Pageable pageable) {

        JPAQuery<Tuple> query = queryFactory.select(item, new CaseBuilder()
                    .when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum(), idolMember, idolGroup, categoryItem, user)
                    .from(item)
                    .groupBy(item, idolMember, idolGroup, categoryItem, user)
                    .leftJoin(userItem).on(userItem.item.id.eq(item.id))
                    .join(idolMember).on(item.idolMember.eq(idolMember))
                    .join(idolGroup).on(idolMember.idolGroup.eq(idolGroup))
                    .join(categoryItem).on(item.categoryItem.eq(categoryItem))
                    .join(user).on(item.user.eq(user));

        pageable.getSort().stream().forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            PathBuilder orderByExpression = new PathBuilder(Item.class, "item");
            query.orderBy(new OrderSpecifier(direction, orderByExpression.get(order.getProperty())));

        });

        return query.fetch();
    }
    @Override
    public Tuple findByIdWithUserItem(Long userId, Long itemId) {
        return queryFactory.select(item, new CaseBuilder().when(userItem.user.id.eq(userId)).then(1L).otherwise(0L).sum())
                .from(item)
                .leftJoin(userItem).on(userItem.item.eq(item))
                .where(item.id.eq(itemId))
                .groupBy(item)
                .fetchOne();
    }

    @Override
    public List<Tuple> findAllByUserIdAndTradeStatus(Long userId, TradeStatus status) {

        QImage subImage = new QImage("subImage");

        JPQLQuery<Long> rankSubquery = JPAExpressions.select(subImage.count().add(1))
                .from(subImage)
                .where(subImage.id.lt(image.id).and(
                        subImage.item.eq(image.item)
                ));

        return queryFactory.select(item, image)
                .from(item)
                .leftJoin(image).on(image.item.id.eq(item.id))
                .where(item.user.id.eq(userId).and(
                        item.tradeStatus.eq(status)
                                .and(
                                rankSubquery.in(1L, null)
                        )
                ))
                .fetch();
    }

    @Override
    public long updateTradeStatus(Long itemId, TradeStatus status) {
        return queryFactory.update(item)
                .set(item.tradeStatus, status)
                .where(item.id.eq(itemId))
                .execute();
    }
}

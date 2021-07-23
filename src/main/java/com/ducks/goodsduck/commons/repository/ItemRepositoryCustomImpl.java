package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
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
    private QPricePropose pricePropose = QPricePropose.pricePropose;

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

    // FEAT : 좋아요, 회원별 좋아하는 아이돌 필터링 기능 적용
    @Override
    public List<Tuple> findAllWithUserItemIdolGroup(Long userId, List<UserIdolGroup> userIdolGroups, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        if(userIdolGroups.size() != 0) {
            for (UserIdolGroup userIdolGroup : userIdolGroups) {
                builder.or(idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
            }
        }

        // HINT : 경원
        return queryFactory
                .select(item, userItem)
                .from(item)
                .leftJoin(userItem).on(userItem.user.id.eq(userId), userItem.item.id.eq(item.id))
                .join(item.idolMember, idolMember)
                .join(item.idolMember.idolGroup, idolGroup)
                .join(item.categoryItem, categoryItem)
                .join(item.user, user)
                .where(builder)
                .orderBy(item.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

        // HINT : 태호
//        return queryFactory.select(item, new CaseBuilder()
//                .when(userItem.user.id.eq(userId)).then(1L)
//                .otherwise(0L)
//                .sum(), idolMember, idolGroup, categoryItem, user)
//                .from(item)
//                .leftJoin(userItem).on(userItem.item.id.eq(item.id))
//                .join(item.idolMember, idolMember)
//                .join(item.idolMember.idolGroup, idolGroup)
//                .join(item.categoryItem, categoryItem)
//                .join(item.user, user)
//                .where(builder)
//                .orderBy(item.createdAt.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize()+1)
//                .fetch();
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
    public List<Tuple> findAllByUserIdAndTradeStatus(Long userId, List<TradeStatus> status) {

        QImage subImage = new QImage("subImage");

        NumberExpression<Integer> tradeStatusCompareExpression = new CaseBuilder()
                .when(item.tradeStatus.eq(TradeStatus.BUYING)).then(20)
                .when(item.tradeStatus.eq(TradeStatus.SELLING)).then(20)
                .when(item.tradeStatus.eq(TradeStatus.RESERVING)).then(30)
                .when(item.tradeStatus.eq(TradeStatus.COMPLETE)).then(10)
                .otherwise(0);

        JPQLQuery<Long> rankOfImageSubquery = JPAExpressions.select(subImage.count().add(1))
                .from(subImage)
                .where(subImage.id.lt(image.id).and(
                        subImage.item.eq(image.item)
                ));

        return queryFactory.select(item, image)
                .from(item)
                .join(image).on(item.eq(image.item))
                .where(item.tradeStatus.in(status)
                        .and(item.user.id.eq(userId).and(
                            rankOfImageSubquery.eq(1L))
                        )
                )
                .orderBy(tradeStatusCompareExpression.desc())
                .orderBy(item.createdAt.desc())
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

package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PriceProposeRepositoryCustomImpl implements PriceProposeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QPricePropose pricePropose = QPricePropose.pricePropose;
    private QUser user = QUser.user;

    public PriceProposeRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public PricePropose findByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory
                .select(pricePropose)
                .from(pricePropose)
                .where(pricePropose.user.id.eq(userId).and(
                        pricePropose.item.id.eq(itemId)
                ).and(pricePropose.status.in(PriceProposeStatus.ACCEPTED, PriceProposeStatus.SUGGESTED)))
                .fetchOne();
    }

    @Override
    public List<PricePropose> findAllByItemId(Long itemId) {
        return queryFactory
                .select(pricePropose)
                .from(pricePropose)
                .where(pricePropose.item.id.eq(itemId)
                .and(pricePropose.status.in(PriceProposeStatus.ACCEPTED, PriceProposeStatus.SUGGESTED)))
                .orderBy(pricePropose.id.desc())
                .fetch();
    }

    @Override
    public List<PricePropose> findAllByItemIdWithAllStatus(Long itemId) {
        return queryFactory
                .select(pricePropose)
                .from(pricePropose)
                .where(pricePropose.item.id.eq(itemId))
                .orderBy(pricePropose.id.desc())
                .fetch();
    }

    @Override
    public Long updatePrice(Long userId, Long priceProposeId, int price) {
        return queryFactory.update(pricePropose)
                .set(pricePropose.price, price)
                .set(pricePropose.createdAt, LocalDateTime.now())
                .where(pricePropose.id.eq(priceProposeId).and(
                        pricePropose.user.id.eq(userId)).and(pricePropose.status.eq(PriceProposeStatus.SUGGESTED))
                )
                .execute();
    }

    @Override
    public List<Tuple> findByItems(List<Item> items) {
        return queryFactory
                .select(pricePropose.item, pricePropose.user, pricePropose)
                .from(pricePropose)
                .where(pricePropose.item.in(items).and(
                        pricePropose.status.eq(PriceProposeStatus.SUGGESTED)
                ))
                .orderBy(pricePropose.id.desc())
                .fetch();
    }

    @Override
    public List<Tuple> findByItemId(Long itemId) {
        return queryFactory
                .select(user, pricePropose.item, pricePropose)
                .from(pricePropose)
                .join(pricePropose.user, user)
                .where(pricePropose.item.id.eq(itemId).and(
                        pricePropose.status.eq(PriceProposeStatus.SUGGESTED)
                ))
                .orderBy(pricePropose.id.desc())
                .fetch();
    }

    @Override
    public List<Tuple> findByUserId(Long userId) {
        return queryFactory.select(pricePropose.item, pricePropose)
                .from(pricePropose)
                .where(pricePropose.user.id.eq(userId).and(
                        pricePropose.status.in(PriceProposeStatus.SUGGESTED, PriceProposeStatus.REFUSED)
                ))
                .orderBy(pricePropose.id.desc())
                .fetch();
    }

    @Override
    public Long updateStatus(Long priceProposeId, PriceProposeStatus status) {
        return queryFactory.update(pricePropose)
                .set(pricePropose.status, status)
                .where(pricePropose.id.eq(priceProposeId).and(
                        pricePropose.status.eq(PriceProposeStatus.SUGGESTED)
                ))
                .execute();
    }

    @Override
    public Long countSuggestedInItems(Long userId) {
        return queryFactory
                .select(pricePropose)
                .from(pricePropose)
                .where(pricePropose.user.id.eq(userId).and(pricePropose.status.eq(PriceProposeStatus.SUGGESTED)))
                .fetchCount();
    }
}

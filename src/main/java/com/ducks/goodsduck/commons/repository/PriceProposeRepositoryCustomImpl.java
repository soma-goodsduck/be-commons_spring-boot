package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.QPricePropose;
import com.ducks.goodsduck.commons.model.entity.QUser;
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
    public List<PricePropose> findByUserIdAndItemId(Long userId, Long itemId) {
        return queryFactory.select(pricePropose)
                .from(pricePropose)
                .where(pricePropose.user.id.eq(userId).and(
                        pricePropose.item.id.eq(itemId)
                ))
                .fetch();
    }

    @Override
    public long updatePrice(Long userId, Long priceProposeId, int price) {
        return queryFactory.update(pricePropose)
                .set(pricePropose.price, price)
                .set(pricePropose.createdAt, LocalDateTime.now())
                .where(pricePropose.id.eq(priceProposeId).and(
                        pricePropose.user.id.eq(userId)
                ))
                .execute();
    }

    @Override
    public List<PricePropose> findByItems(List<Item> items) {
        return queryFactory.selectFrom(pricePropose)
                .where(pricePropose.item.in(items))
                .fetch();
    }

    @Override
    public List<Tuple> findByItemId(Long itemId) {
        return queryFactory.select(user, pricePropose)
                .from(pricePropose)
                .join(pricePropose.user, user)
                .where(pricePropose.item.id.eq(itemId))
                .fetch();
    }

    @Override
    public long updateStatus(Long priceProposeId, PriceProposeStatus status) {
        return queryFactory.update(pricePropose)
                .set(pricePropose.status, status)
                .where(pricePropose.id.eq(priceProposeId))
                .execute();
    }
}

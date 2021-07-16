package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.entity.QPricePropose;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class PriceProposeRepositoryCustomImpl implements PriceProposeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QPricePropose pricePropose = QPricePropose.pricePropose;

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
}

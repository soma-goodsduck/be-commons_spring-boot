package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QReview review = QReview.review;
    private QItem item = QItem.item;
    private QUser user = QUser.user;

    public ReviewRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public boolean existsByItemIdAndUserId(Long itemId, Long senderId) {
        long fetchCount = queryFactory
                .select(review)
                .from(review)
                .where(review.item.id.eq(itemId).and(
                        review.user.id.eq(senderId)
                )).fetchCount();
        return fetchCount > 0L;
    }

    @Override
    public List<Tuple> findInItems(List<Item> items) {
        return queryFactory
                .select(review, user)
                .from(review)
                .join(user).on(review.user.eq(user))
                .where(review.item.in(items))
                .fetch();
    }

    @Override
    public List<Review> findAllByUserId(Long userId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Long countByUserId(Long userId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.user.id.eq(userId))
                .fetchCount();
    }


}

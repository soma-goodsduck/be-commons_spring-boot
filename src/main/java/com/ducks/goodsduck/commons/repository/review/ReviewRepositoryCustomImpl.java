package com.ducks.goodsduck.commons.repository.review;

import com.ducks.goodsduck.commons.model.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QReview review = QReview.review;

    public ReviewRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public boolean existsByItemIdAndSenderIdAndReceiverId(Long itemId, Long senderId, Long receiverId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.item.id.eq(itemId).and(
                        review.user.id.eq(senderId).and(
                        review.receiverId.eq(receiverId))
                )).fetchCount() > 0L;
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.user.id.eq(userId))
                .fetch();
    }

    @Override
    public Long countBySenderId(Long senderId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.user.id.eq(senderId))
                .fetchCount();
    }

    @Override
    public List<Review> findByReveiverId(Long receiverId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.receiverId.eq(receiverId))
                .orderBy(review.id.desc())
                .fetch();
    }

    @Override
    public Long countByReveiverId(Long receiverId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.receiverId.eq(receiverId))
                .fetchCount();
    }

    @Override
    public List<Review> findByItemId(Long itemId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.item.id.eq(itemId))
                .fetch();
    }

    @Override
    public Review findByReveiverIdAndItemId(Long receiverId, Long itemId) {
        return queryFactory
                .select(review)
                .from(review)
                .where(review.receiverId.eq(receiverId).and(
                        review.item.id.eq(itemId)))
                .fetchOne();
    }
}

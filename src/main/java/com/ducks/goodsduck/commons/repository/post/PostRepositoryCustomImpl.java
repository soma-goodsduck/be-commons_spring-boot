package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.QPost;
import com.ducks.goodsduck.commons.model.entity.QUserPost;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static com.ducks.goodsduck.commons.model.entity.QPost.post;
import static com.ducks.goodsduck.commons.model.entity.QUser.user;
import static com.ducks.goodsduck.commons.model.entity.QUserPost.userPost;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Tuple findByIdWithUserPost(Long userId, Long postId) {
        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(postId))
                .where(post.id.eq(postId))
                .fetchOne();
    }
}

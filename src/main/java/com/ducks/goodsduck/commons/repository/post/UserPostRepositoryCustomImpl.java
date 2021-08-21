package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.QUser;
import com.ducks.goodsduck.commons.model.entity.QUserPost;
import com.ducks.goodsduck.commons.model.entity.UserPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.ducks.goodsduck.commons.model.entity.QUser.user;
import static com.ducks.goodsduck.commons.model.entity.QPost.post;
import static com.ducks.goodsduck.commons.model.entity.QUserPost.userPost;

@Repository
public class UserPostRepositoryCustomImpl implements UserPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserPostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public UserPost findByUserIdAndPostId(Long userId, Long postId) {
        return queryFactory
                .select(userPost)
                .from(userPost)
                .where(userPost.user.id.eq(userId).and(userPost.post.id.eq(postId)))
                .fetchOne();
    }
}

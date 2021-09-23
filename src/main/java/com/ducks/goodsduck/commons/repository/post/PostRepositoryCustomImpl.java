package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.QIdolGroup;
import com.ducks.goodsduck.commons.model.entity.QPost;
import com.ducks.goodsduck.commons.model.entity.QUserPost;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.ducks.goodsduck.commons.model.entity.QIdolGroup.idolGroup;
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

    @Override
    public List<Tuple> findBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        for (UserIdolGroup userIdolGroup : userIdolGroups) {
            builder.or(post.idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
        }

        if(!postId.equals(0L)) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(postId))
                .where(builder)
                .orderBy(post.id.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!postId.equals(0L)) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(postId))
                .where(post.idolGroup.id.eq(idolGroupId).and(builder))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.PAGEABLE_SIZE + 1)
                .fetch();
    }
}

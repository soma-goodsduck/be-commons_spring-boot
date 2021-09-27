package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.category.QCategory;
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
import static com.ducks.goodsduck.commons.model.entity.category.QCategory.*;

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

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(post.id))
                .where(builder)
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findFreeBylikeIdolGroupsWithUserPost(Long userId, List<UserIdolGroup> userIdolGroups, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        for (UserIdolGroup userIdolGroup : userIdolGroups) {
            builder.or(post.idolGroup.id.eq(userIdolGroup.getIdolGroup().getId()));
        }

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(post.id))
                .where(builder.and(post.postCategory.name.eq("나눔글")))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(post.id))
                .where(post.idolGroup.id.eq(idolGroupId).and(builder))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Tuple> findFreeByUserIdolGroupWithUserPost(Long userId, Long idolGroupId, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post, userPost)
                .from(post)
                .leftJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(post.id))
                .where(post.idolGroup.id.eq(idolGroupId).and(builder).and(post.postCategory.name.eq("나눔글")))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Post> findByUserId(Long userId, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post)
                .from(post)
                .where(builder.and(post.user.id.eq(userId)).and(post.deletedAt.isNull()))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<Post> findAllWithUserPost(Long userId, Long postId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(postId != 0) {
            builder.and(post.id.lt(postId));
        }

        return queryFactory
                .select(post)
                .from(post)
                .innerJoin(userPost).on(userPost.user.id.eq(userId), userPost.post.id.eq(post.id))
                .where(builder.and(userPost.deletedAt.isNull()))
                .orderBy(post.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }
}

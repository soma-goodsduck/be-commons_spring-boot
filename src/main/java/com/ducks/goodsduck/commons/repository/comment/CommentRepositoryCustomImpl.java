package com.ducks.goodsduck.commons.repository.comment;

import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.QComment;
import com.ducks.goodsduck.commons.model.entity.QPost;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.ducks.goodsduck.commons.model.entity.QComment.comment;
import static com.ducks.goodsduck.commons.model.entity.QPost.post;

@Repository
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CommentRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return queryFactory
                .select(comment)
                .from(comment)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.parentComment.id.asc().nullsFirst(), comment.id.asc())
                .fetch();
    }

    @Override
    public List<Comment> findTopCommentsByPostId(Long postId) {
        return queryFactory
                .select(comment)
                .from(comment)
                .where(comment.post.id.eq(postId).and(comment.receiveCommentId.isNull()))
                .orderBy(comment.createdAt.asc())
                .fetch();
    }

    @Override
    public List<Comment> findByUserId(Long userId, Long commentId) {

        BooleanBuilder builder = new BooleanBuilder();

        if(commentId != 0) {
            builder.and(comment.id.lt(commentId));
        }

        return queryFactory
                .select(comment)
                .from(comment)
                .where(builder.and(comment.user.id.eq(userId)).and(comment.deletedAt.isNull()))
                .orderBy(comment.id.desc())
                .limit(PropertyUtil.POST_PAGEABLE_SIZE + 1)
                .fetch();
    }
}

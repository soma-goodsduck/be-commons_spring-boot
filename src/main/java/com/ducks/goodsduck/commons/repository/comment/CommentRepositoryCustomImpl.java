package com.ducks.goodsduck.commons.repository.comment;

import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.QComment;
import com.ducks.goodsduck.commons.model.entity.QPost;
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
}

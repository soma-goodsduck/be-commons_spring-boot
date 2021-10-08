package com.ducks.goodsduck.commons.repository.comment;

import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.Post;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepositoryCustom {

    // 댓글 목록 조회 V1, V2
    List<Comment> findAllByPostId(Long postId);

    // 댓글 목록 조회 V3
    List<Comment> findTopCommentsByPostId(Long postId);
    
    // 내가 작성한 댓글 목록 조회 V1
    List<Comment> findByUserId(Long userId, Long commentId);

    // 내가 작성한 댓글 목록의 게시글 조회 V2
    List<Tuple> findByUserIdV2(Long userId, Long commentId);
}
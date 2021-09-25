package com.ducks.goodsduck.commons.repository.comment;

import com.ducks.goodsduck.commons.model.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepositoryCustom {

    List<Comment> findAllByPostId(Long postId);
    
    // 내가 작성한 댓글 목록 조회
    List<Comment> findByUserId(Long userId, Long commentId);
}
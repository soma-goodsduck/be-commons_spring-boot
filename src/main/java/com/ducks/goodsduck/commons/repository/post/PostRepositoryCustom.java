package com.ducks.goodsduck.commons.repository.post;

import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepositoryCustom {

    // 좋아요 확인
    Tuple findByIdWithUserPost(Long userId, Long postId);
}

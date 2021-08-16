package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.UserPost;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPostRepositoryCustom {

    UserPost findByUserIdAndPostId(Long userId, Long postId);
}

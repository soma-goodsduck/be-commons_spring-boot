package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.UserPost;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPostRepositoryCustom {

    UserPost findByUserIdAndPostId(Long userId, Long postId);
//    List<UserPost> findByPostId(Long postId);
}

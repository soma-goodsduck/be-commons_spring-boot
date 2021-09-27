package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.UserItem;
import com.ducks.goodsduck.commons.model.entity.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPostRepository extends JpaRepository<UserPost, Long> {

    List<UserPost> findAllByPostId(Long postId);
    List<UserPost> findAllByUserId(Long userId);
}

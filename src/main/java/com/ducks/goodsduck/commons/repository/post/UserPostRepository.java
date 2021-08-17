package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPostRepository extends JpaRepository<UserPost, Long> {
}

package com.ducks.goodsduck.commons.repository.post;

import com.ducks.goodsduck.commons.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserId(Long userId);

    @Query("select p from Post p where p.deletedAt is not null")
    List<Post> findAllWithDeleted();
}

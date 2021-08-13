package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

}

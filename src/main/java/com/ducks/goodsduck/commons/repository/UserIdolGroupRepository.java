package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIdolGroupRepository extends JpaRepository<UserIdolGroup, Long> {
}

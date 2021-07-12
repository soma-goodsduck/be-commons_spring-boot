package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdolGroupRepository extends JpaRepository<IdolGroup, Long> {
}

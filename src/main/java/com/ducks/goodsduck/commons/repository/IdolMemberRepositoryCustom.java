package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.IdolMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdolMemberRepositoryCustom {
    List<IdolMember> findAllByIdolGroupId(Long IdolGroupId);
}
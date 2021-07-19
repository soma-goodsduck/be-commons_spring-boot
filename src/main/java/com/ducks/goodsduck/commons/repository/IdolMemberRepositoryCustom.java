package com.ducks.goodsduck.commons.repository;

import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdolMemberRepositoryCustom {
    List<Tuple> findAll();
    List<Tuple> findAllByIdolGroupId(Long IdolGroupId);
}

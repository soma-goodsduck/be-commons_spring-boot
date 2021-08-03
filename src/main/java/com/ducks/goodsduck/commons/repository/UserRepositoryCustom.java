package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositoryCustom {

    Long findByNickname(String nickname);
}

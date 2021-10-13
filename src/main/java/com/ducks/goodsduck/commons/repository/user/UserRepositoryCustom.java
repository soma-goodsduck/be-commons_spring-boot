package com.ducks.goodsduck.commons.repository.user;

import com.ducks.goodsduck.commons.model.enums.UserRole;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositoryCustom {
    Long updateRoleByUserId(Long userId, UserRole role);
    Long initializeVotedIdolGroupIdAll();
    Long addDailyVoteAll();
}

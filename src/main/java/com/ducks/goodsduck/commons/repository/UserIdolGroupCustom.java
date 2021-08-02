package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;

public interface UserIdolGroupCustom {

    UserIdolGroup findByUserIdAndIdolGroupId(Long userId, Long idolGroupId);
}

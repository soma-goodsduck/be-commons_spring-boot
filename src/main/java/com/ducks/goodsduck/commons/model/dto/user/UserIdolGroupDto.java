package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import lombok.Data;

@Data
public class UserIdolGroupDto {

    private Long idolGroupId;
    private String idolGroupName;

    public UserIdolGroupDto(UserIdolGroup userIdolGroup) {
        this.idolGroupId = userIdolGroup.getIdolGroup().getId();
        this.idolGroupName = userIdolGroup.getIdolGroup().getName();
    }
}

package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {

    String nickName;
    private List<Long> likeIdolGroupsId = new ArrayList<>();
}

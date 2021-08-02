package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class UserIdolGroupUpdateRequest {

    private List<Long> likeIdolGroupsId = new ArrayList<>();
}

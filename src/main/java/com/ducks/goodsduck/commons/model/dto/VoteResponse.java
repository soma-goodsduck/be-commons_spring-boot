package com.ducks.goodsduck.commons.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoteResponse {

    private Long idolGroupId;
    private Long voteCount;
}

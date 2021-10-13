package com.ducks.goodsduck.commons.model.dto.idol;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class IdolGroupWithVotes {

    private List<IdolGroupWithVote> idolGroups;
    private Long haveVoteCount;

    public IdolGroupWithVotes(List<IdolGroupWithVote> idolGroupWithVoteList, Long haveVoteCount) {
        this.idolGroups = idolGroupWithVoteList;
        this.haveVoteCount = haveVoteCount;
    }
}

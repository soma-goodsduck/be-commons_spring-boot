package com.ducks.goodsduck.commons.model.dto.user;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVoteResponse {
    private Long votedIdolGroupId;
}

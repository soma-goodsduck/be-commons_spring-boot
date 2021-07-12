package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import lombok.Data;

@Data
public class IdolGroupDto {

    private Long id;
    private String engName;
    private String korName;
    private long votedCount;
    private String imageUrl;

    public IdolGroupDto(IdolGroup idolGroup) {
        this.id = idolGroup.getId();
        this.engName = idolGroup.getEngName();
        this.korName = idolGroup.getKorName();
        this.votedCount = idolGroup.getVotedCount();
        this.imageUrl = idolGroup.getImageUrl();
    }
}

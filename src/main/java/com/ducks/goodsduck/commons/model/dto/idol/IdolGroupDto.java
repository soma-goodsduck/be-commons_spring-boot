package com.ducks.goodsduck.commons.model.dto.idol;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdolGroupDto {

    private Long id;
    private String name;
    private String engName;
    private String korName;
    private String imageUrl;

    public IdolGroupDto(IdolGroup idolGroup) {
        this.id = idolGroup.getId();
        this.name = idolGroup.getName();
        this.engName = idolGroup.getEngName();
        this.korName = idolGroup.getKorName();
        this.imageUrl = idolGroup.getImageUrl();
    }
}

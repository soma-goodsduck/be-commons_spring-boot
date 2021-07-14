package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.IdolMember;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class IdolMemberDto {

    private Long id;
    private String name;
    private String imageUrl;
    private String idolGroupEngName;
    private String idolGroupKorName;

    public IdolMemberDto(IdolMember idolMember) {
        this.id = idolMember.getId();
        this.name = idolMember.getName();
        this.imageUrl = idolMember.getImageUrl();
        this.idolGroupEngName = idolMember.getIdolGroup().getEngName();
        this.idolGroupKorName = idolMember.getIdolGroup().getKorName();
    }
}

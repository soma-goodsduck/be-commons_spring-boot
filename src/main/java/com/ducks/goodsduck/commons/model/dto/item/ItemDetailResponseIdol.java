package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.IdolMember;
import lombok.Data;

@Data
public class ItemDetailResponseIdol {

    private String groupName;
    private String memberName;

    public ItemDetailResponseIdol(IdolMember idolMember) {
        this.memberName = idolMember.getName();
        this.groupName = idolMember.getIdolGroup().getKorName();
    }
}
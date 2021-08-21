package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.entity.IdolMember;
import lombok.Data;

@Data
public class ItemDetailResponseIdol {

    private Long groupId;
    private String groupName;
    private Long memberId;
    private String memberName;

    public ItemDetailResponseIdol(IdolMember idolMember) {

        this.groupId = idolMember.getIdolGroup().getId();
        this.groupName = idolMember.getIdolGroup().getName();
        this.memberId = idolMember.getId();
        this.memberName = idolMember.getName();
    }
}
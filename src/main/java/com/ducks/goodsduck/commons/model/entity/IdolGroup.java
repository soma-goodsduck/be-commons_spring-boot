package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdolGroup {

    @Id @GeneratedValue
    @Column(name = "idol_group_id")
    private Long id;
    private String korName;
    private String engName;
    private Long votedCount;
    private String imageUrl;
}

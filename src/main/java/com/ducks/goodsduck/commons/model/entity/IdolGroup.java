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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDOL_GROUP_ID")
    private Long id;
    private String engName;
    private String korName;
    private long votedCount;
    private String imageUrl;
}

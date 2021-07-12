package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdolGroup {

    @Id @GeneratedValue
    @Column(name = "IDOL_GROUP_ID")
    private Long id;
    private String engName;
    private String korName;
    private long votedCount;
    private String imageUrl;
}

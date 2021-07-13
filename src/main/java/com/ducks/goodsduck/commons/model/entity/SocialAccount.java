package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.SocialType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Enumerated(EnumType.STRING)
    private SocialType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public SocialAccount(String id, SocialType type) {
        this.id = id;
        this.type = type;
    }
}

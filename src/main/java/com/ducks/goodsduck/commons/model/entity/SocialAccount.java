package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.SocialType;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private SocialType type;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public SocialAccount(String id, SocialType type) {
        this.id = id;
        this.type = type;
    }
}

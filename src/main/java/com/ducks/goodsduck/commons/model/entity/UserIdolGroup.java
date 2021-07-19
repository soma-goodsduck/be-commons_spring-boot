package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdolGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_IDOL_GROUP_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDOL_GROUP_ID")
    private IdolGroup idolGroup;

    public UserIdolGroup(User user, IdolGroup idolGroup) {
        this.user = user;
        this.idolGroup = idolGroup;
    }
}

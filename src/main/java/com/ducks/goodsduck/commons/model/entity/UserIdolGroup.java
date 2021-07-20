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
    @Column(name = "user_idol_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_group_id")
    private IdolGroup idolGroup;

    public static UserIdolGroup createUserIdolGroup(IdolGroup idolGroup) {
        UserIdolGroup userIdolGroup = new UserIdolGroup();
        userIdolGroup.setIdolGroup(idolGroup);
        return userIdolGroup;
    }
}

package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id; // user_id
    private String nickName;
    private String email;
    private String phoneNumber;
    private String imageUrl;

    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccount;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
    }
}

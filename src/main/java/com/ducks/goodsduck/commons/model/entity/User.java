package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User(UserRole role) {
        this.role = this.role;
    }

    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = UserRole.USER;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccount.setUser(this);
        socialAccounts.add(socialAccount);
    }

    public User login() {
        this.lastLoginAt = LocalDateTime.now();
        return this;
    }
}

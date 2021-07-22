package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<UserIdolGroup> userIdolGroups = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Address> addresses = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.lastLoginAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.role = UserRole.USER;
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccount.setUser(this);
        socialAccounts.add(socialAccount);
    }

    public void addUserIdolGroup(UserIdolGroup userIdolGroup) {
        userIdolGroup.setUser(this);
        userIdolGroups.add(userIdolGroup);
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
    }
}

package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String bcryptId;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserIdolGroup> userIdolGroups = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Address> addresses = new ArrayList<>();

    // TODO : 없애도 될듯
    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();


    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.lastLoginAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.role = UserRole.USER;
        this.bcryptId = createBcryptId();
    }

    public String createBcryptId() {
        Random random = new Random();
        int number = random.nextInt(26) + 65;

        String tempBcryptId = BCrypt.hashpw(this.phoneNumber, BCrypt.gensalt(5));
        return tempBcryptId.replace('/', (char)number);
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

package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.annotation.IntegerArrayConverter;
import com.ducks.goodsduck.commons.model.enums.ActivityType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    private String password;
    private String phoneNumber;
    private String imageUrl;
    private Integer level;
    private Integer exp;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Boolean haveGetGrantOfAttend;
    private Long votedIdolGroupId;
    private Long numberOfVotes;

    private LocalDateTime deletedAt;
    private Boolean marketingAgree;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserIdolGroup> userIdolGroups = new ArrayList<>();

    @Convert(converter = IntegerArrayConverter.class)
    private List<Long> blockedUserIds = new ArrayList<>();

    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.role = UserRole.USER;
        this.level = 1;
        this.exp = 0;
        this.reportCount = 0;
        this.bcryptId = createBcryptId();
        this.haveGetGrantOfAttend = false;
        this.votedIdolGroupId = 0L;
        this.numberOfVotes = 2L;
    }

    public String createBcryptId() {
        Random random = new Random();
        int number = random.nextInt(26) + 65;

        String tempBcryptId = BCrypt.hashpw(this.phoneNumber, BCrypt.gensalt(5));
        return tempBcryptId.replace('/', (char)number);
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccount.setUser(this);
    }

    public void addUserIdolGroup(UserIdolGroup userIdolGroup) {
        userIdolGroup.setUser(this);
        userIdolGroups.add(userIdolGroup);
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void vote(Long idolGroupId, Long voteCount) {
        this.votedIdolGroupId = idolGroupId;
        this.numberOfVotes -= voteCount;
    }

    public Integer gainExpByType(ActivityType activityType) {
        this.exp += activityType.getExp();
        return exp;
    }

    public boolean levelUp() {
        if(this.level + 1 <= 25) {
            this.level++;
            this.exp -= 100;
            return true;
        } else {
            return false;
        }
    }

    public void getVoteByActivity(ActivityType activityType){
        this.numberOfVotes += activityType.getVote();
    }

    public void grantOfAttend() {
        this.numberOfVotes += 2L;
        this.haveGetGrantOfAttend = true;
    }
}

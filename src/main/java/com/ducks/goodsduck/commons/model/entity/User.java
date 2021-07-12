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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "ITEM_ID")
    private List<Item> items = new ArrayList<Item>();

    /** user_item 다대다 식별 관계 정의 */
    @ManyToMany
    @JoinTable(name = "USER_ITEM",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ITEM_ID"))
    private List<Item> likeItems = new ArrayList<Item>();

    /** user_idol_group 다대다 식별 관계 정의 */
    @ManyToMany
    @JoinTable(name = "USER_IDOL_GROUP",
                joinColumns = @JoinColumn(name = "USER_ID"),
                inverseJoinColumns = @JoinColumn(name = "IDOL_GROUP_ID"))
    private List<IdolGroup> likeIdolGroups = new ArrayList<IdolGroup>();

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User(String nickName, String email, String phoneNumber) {
        this.nickName = nickName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.role = UserRole.USER;
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccount.setUser(this);
        socialAccounts.add(socialAccount);
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}

package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserDtoV2 {

    private Boolean emailSuccess;
    private Boolean passwordSuccess;
    private String bcryptId;
    private String nickName;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private String jwt;
    private UserRole role;
    private Integer level;
    private Integer exp;
    private List<UserIdolGroupDto> likeIdolGroups = new ArrayList<>();
    private Boolean isAgreeToNotification;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime deletedAt;

    public void setAgreeToNotification(Boolean isAgree) {
        this.isAgreeToNotification = isAgree;
    }

    public UserDtoV2(User user) {
        this.nickName = user.getNickName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.role = user.getRole();
        this.level = user.getLevel();
        this.exp = user.getExp();
        this.likeIdolGroups = user.getUserIdolGroups().stream()
                                .map(userIdolGroup -> new UserIdolGroupDto(userIdolGroup))
                                .collect(Collectors.toList());
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.jwt = "";
        this.bcryptId = user.getBcryptId();
        this.isAgreeToNotification = true;
    }
}

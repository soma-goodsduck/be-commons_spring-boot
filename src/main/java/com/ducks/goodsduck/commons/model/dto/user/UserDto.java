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
public class UserDto {

    private SocialType socialType;
    private String socialAccountId;
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

    public static UserDto createUserDto(UserRole role) {
        UserDto userDto = new UserDto();
        userDto.setRole(role);
        return userDto;
    }

    public void setAgreeToNotification(Boolean isAgree) {
        this.isAgreeToNotification = isAgree;
    }

    public UserDto(User user) {
        this.nickName = user.getNickName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.role = user.getRole();
        this.level = user.getLevel();
        this.exp = user.getLevel();
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

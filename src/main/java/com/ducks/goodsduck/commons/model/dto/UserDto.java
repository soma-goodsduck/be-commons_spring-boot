package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDto {

    private String nickName;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private String jwt;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserDto createUserDto(UserRole role) {
        var userDto = new UserDto();
        userDto.setRole(role);
        return userDto;
    }

    public UserDto(User user) {
        this.nickName = user.getNickName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.jwt = "";
    }
}

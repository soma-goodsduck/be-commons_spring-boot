package com.ducks.goodsduck.commons.model;

import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    private String nickName;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public UserDto() {
    }

    public UserDto(User user) {
        this.nickName = user.getNickName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }

}

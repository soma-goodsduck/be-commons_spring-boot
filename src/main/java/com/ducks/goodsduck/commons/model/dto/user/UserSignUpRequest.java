package com.ducks.goodsduck.commons.model.dto.user;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원 가입 시
 * 소셜 로그인 정보 + 닉네임, 이메일, 핸드폰 번호 입력 받는 DTO
 */

@Data
public class UserSignUpRequest {

    private String email;
    private String phoneNumber;
    private String nickName;
    private List<Long> likeIdolGroupsId = new ArrayList<>();
    private String socialAccountId;
    private SocialType socialAccountType;
}
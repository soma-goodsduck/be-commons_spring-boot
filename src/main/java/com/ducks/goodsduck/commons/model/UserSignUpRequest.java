package com.ducks.goodsduck.commons.model;

import lombok.Data;

/**
 * 회원 가입 시
 * 소셜 로그인 정보 + 닉네임, 이메일, 핸드폰 번호 입력 받는 DTO
 */

@Data
public class UserSignUpRequest {

    private String socialAccountId;
    private String socialAccountType;
    private String nickName;
    private String email;
    private String phoneNumber;


}

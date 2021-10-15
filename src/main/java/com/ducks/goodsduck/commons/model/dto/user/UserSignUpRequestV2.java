package com.ducks.goodsduck.commons.model.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 가입 시
 * 소셜 로그인 정보 + 닉네임, 이메일, 핸드폰 번호 입력 받는 DTO
 */

@Data
public class UserSignUpRequestV2 {

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "invalid email format.")
    private String email;

    @NotBlank(message = "Password must not be blank.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@\\#$%<>^&*_-])[A-Za-z\\d$@$!%*#?&]{8,}$", message = "Password must include alphabat, number, special symbol")
    @Size(min=8, max = 20, message = "Password length must be between 8 and 20.")
    private String password;

    @NotBlank(message = "Phone number must not be blank.")
    @Pattern(regexp = "(01[016789])(\\d{3,4})(\\d{4})", message = "Invalid phone number format.")
    private String phoneNumber;

    @NotBlank(message = "Nick name must not be blank.")
    @Size(max = 20, message = "")
    private String nickName;

    private List<Long> likeIdolGroupsId = new ArrayList<>();
    private Boolean marketingAgree;
}
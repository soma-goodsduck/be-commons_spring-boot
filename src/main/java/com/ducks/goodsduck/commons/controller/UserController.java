package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.SocialAccountDto;
import com.ducks.goodsduck.commons.model.dto.UserDto;
import com.ducks.goodsduck.commons.model.dto.UserSignUpRequest;
import com.ducks.goodsduck.commons.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    /** 소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API */
    @GetMapping("/oauth2/authorization/naver")
    public SocialAccountDto oauth2AuthorizationNaver(
                                         @RequestParam("code") String code,
                                         @RequestParam("state") String state) {
        return userService.oauth2AuthorizationNaver(code, state);
    }

    @GetMapping("/user")
    public List<UserDto> getUserList() {
        return userService.findAll();
    }

    @GetMapping("/user/{user_id}")
    public UserDto getUser(@RequestParam Long user_id) {
        return userService.find(user_id);
    }

    /** 회원가입 API */
    @PostMapping("/signup")
    public UserDto userSignUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        return userService.signUp(userSignUpRequest);
    }

    /** 로그인 체크 API */
    @GetMapping("/login")
    public UserDto login(@RequestParam("token") String token) {
        return userService.checkLoginStatus(token);
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.UserDto;
import com.ducks.goodsduck.commons.model.dto.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    /** 소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API */
    @GetMapping("/login/naver")
    public UserDto authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return userService.oauth2AuthorizationNaver(code, state);
    }

    /** 소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 API */
    @GetMapping("/login/kakao")
    public UserDto authorizeKakao(@RequestParam("code") String code) {
        return userService.oauth2AuthorizationKakao(code);
    }

    /** 회원가입 API */
    @PostMapping("/signup")
    public UserDto signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return userService.signUp(userSignUpRequest);
    }

    /** JWT를 통한 권한체크 및 JWT 갱신 */
    @GetMapping("/validate/user")
    public UserDto validateUser(@RequestHeader("token") String token) {
        return userService.checkLoginStatus(token);
    }

    @GetMapping("/user")
    public List<UserDto> getUserList() {
        return userService.findAll();
    }

    @GetMapping("/user/{user_id}")
    public UserDto getUser(@RequestParam Long user_id) {
        return userService.find(user_id)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)); // user를 못찾으면 빈 UserDto(UserRole.ANONYMOUS) 반환
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/login/naver")
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API")
    public UserDto authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return userService.oauth2AuthorizationNaver(code, state);
    }

    @GetMapping("/login/kakao")
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 API")
    public UserDto authorizeKakao(@RequestParam("code") String code) {
        return userService.oauth2AuthorizationKakao(code);
    }

    @PostMapping("/signup")
    @ApiOperation("회원가입 API")
    public UserDto signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return userService.signUp(userSignUpRequest);
    }

    @NoCheckJwt
    @GetMapping("/validate/user")
    @ApiOperation("JWT를 통한 권한체크 및 JWT 갱신 API")
    public UserDto validateUser(@RequestHeader("jwt") String jwt) {
        return userService.checkLoginStatus(jwt);
    }

    @GetMapping("/user")
    @ApiOperation("(개발용) 모든 유저 정보 조회 API")
    public List<UserDto> getUserList() {
        return userService.findAll();
    }

    @GetMapping("/user/{user_id}")
    @ApiOperation("(개발용) 특정 유저 정보 조회 API")
    public UserDto getUser(@RequestParam Long user_id) {
        return userService.find(user_id)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)); // user를 못찾으면 빈 UserDto(UserRole.ANONYMOUS) 반환
    }
}

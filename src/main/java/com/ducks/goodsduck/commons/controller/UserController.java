package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.SocialAccountDto;
import com.ducks.goodsduck.commons.model.dto.UserDto;
import com.ducks.goodsduck.commons.model.dto.UserSignUpRequest;
import com.ducks.goodsduck.commons.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://db7ce5a8bb28d2.localhost.run", allowCredentials = "true")
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

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

    /**
     * 회원가입
     * Request :
     */
    @PostMapping("/signup")
    public UserDto userSignUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        return userService.signUp(userSignUpRequest);
    }

    @GetMapping("/login")
    public UserDto login(@RequestParam("token") String token) {
        return userService.checkLoginStatus(token);
    }
}

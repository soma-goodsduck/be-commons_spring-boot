package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.SocialAccountDto;
import com.ducks.goodsduck.commons.model.UserDto;
import com.ducks.goodsduck.commons.model.UserSignUpRequest;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
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

}

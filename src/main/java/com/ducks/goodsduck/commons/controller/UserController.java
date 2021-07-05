package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    @GetMapping("/oauth2/authorization/naver")
    public String oauth2AuthorizationNaver(
                                         @RequestParam("code") String code,
                                         @RequestParam("state") String state
    ) {
        System.out.println("naver 로그인 컨트롤러 요청!!");
        System.out.println("redirect-uri:" + PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.redirect-uri"));

        return userService.oauth2AuthoriationNaver(code, state);
    }

    @GetMapping("")
    public void home() {

    }
}

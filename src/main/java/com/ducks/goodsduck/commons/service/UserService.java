package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.domain.AuthorizationNaver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OauthNaverService oauthNaverService;

    // 네이버로 인증받기
    public String oauth2AuthoriationNaver(String code, String state) {
        AuthorizationNaver authorizationNaver = oauthNaverService.callTokenApi(code, state);
        String userInfoFromNaver = oauthNaverService.callGetUserByAccessToken(authorizationNaver.getAccess_token());

        System.out.println("userInfoFromNaver = " + userInfoFromNaver);
        return userInfoFromNaver;
    }
}

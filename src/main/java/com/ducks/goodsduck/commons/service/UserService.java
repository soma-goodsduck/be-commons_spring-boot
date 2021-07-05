package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.AuthorizationNaverDto;
import com.ducks.goodsduck.commons.model.SocialAccountDto;
import com.ducks.goodsduck.commons.model.UserDto;
import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.repository.SocialAccountRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OauthNaverService oauthNaverService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    // 네이버로 인증받기
    public SocialAccountDto oauth2AuthorizationNaver(String code, String state) {
        AuthorizationNaverDto authorizationNaverDto = oauthNaverService.callTokenApi(code, state);

        // 소셜로그인 정보
        String userInfoFromNaver = oauthNaverService.callGetUserByAccessToken(authorizationNaverDto.getAccess_token());

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);
        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = (String) jsonResponseInfo.get("id");

        return socialAccountRepository.existsById(userSocialAccountId) ?
                new SocialAccountDto(userSocialAccountId, true) :
                new SocialAccountDto(userSocialAccountId, false);
    }

    // 유저 전체 리스트 조회
    public List<UserDto> findAll(){
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    // 회원가입
    public UserDto signUp(UserSignUpRequest userSignUpRequest) {
        System.out.println("userSignUpRequest = " + userSignUpRequest);
        User savedUser = userRepository.save(
                new User(userSignUpRequest.getNickName(),
                        userSignUpRequest.getEmail(),
                        userSignUpRequest.getPhoneNumber())
        );

        SocialType socialType = SocialType.NAVER;

        switch (userSignUpRequest
                .getSocialAccountType()
                .toLowerCase()) {
            case "naver":
                break;

            case "kakao":
                socialType = SocialType.KAKAO;
                break;
        }

        socialAccountRepository.save(
                new SocialAccount(
                        userSignUpRequest.getSocialAccountId(),
                        socialType,
                        savedUser
                )
        );

        return new UserDto(savedUser);
    }

    public UserDto find(Long user_id) {
        return userRepository.findById(user_id)
                .map(UserDto::new)
                .orElseGet(UserDto::new); // user를 못찾으면 빈 UserDto 반환 (임시)
    }
}

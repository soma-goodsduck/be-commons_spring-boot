package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.SocialAccountRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OauthNaverService oauthNaverService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtService jwtService;

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
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
    }

    // 회원가입
    public UserDto signUp(UserSignUpRequest userSignUpRequest) {
        System.out.println("userSignUpRequest = " + userSignUpRequest);

        SocialType socialType = SocialType.NAVER;

        //TODO: null로 이루어진 데이터 모두 insert 되는 예외 처리하기
        switch (userSignUpRequest
                .getSocialAccountType()
                .toLowerCase()) {
            case "naver":
                break;

            case "kakao":
                socialType = SocialType.KAKAO;
                break;
        }

        SocialAccount socialAccount = new SocialAccount(userSignUpRequest.getSocialAccountId(), socialType);
        User user = new User(userSignUpRequest.getNickName(),
                userSignUpRequest.getEmail(),
                userSignUpRequest.getPhoneNumber());
        user.addSocialAccount(socialAccount);

        User savedUser = userRepository.save(user);
        socialAccountRepository.save(socialAccount);
        for (SocialAccount sc: savedUser.getSocialAccounts()) {
            System.out.println("sc = " + sc);
        }
        UserDto userDto = new UserDto(user);
        userDto.registerJwt(
                jwtService.createToken("for user check", 1000000, new JwtDto(savedUser.getId()))
        );
        return userDto;
    }

    public UserDto find(Long user_id) {
        return userRepository.findById(user_id)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS));
                 // user를 못찾으면 빈 UserDto(UserRole.ANONYMOUS) 반환 (임시)
    }

    //TODO: 로그인 로직 추가하기

    //TODO: 유저 권한 체크 (로그인 상태 여부)
    public UserDto checkLoginStatus(String token) {
        Map<String, Object> payloads = jwtService.getPayloads(token);
        int userId = (int)payloads.get("userId"); // (need check) 이 과정이 의미가 있는지..
        return userRepository.findById(Long.valueOf(userId))
                .map(user -> user.login()) // lastLoginAt 갱신
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS));
    }
}

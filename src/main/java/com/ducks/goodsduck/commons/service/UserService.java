package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationKakaoDto;
import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationNaverDto;
import com.ducks.goodsduck.commons.model.dto.user.JwtDto;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.SocialAccountRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final CustomJwtService jwtService;
    private final OauthKakaoService oauthKakaoService;
    private final OauthNaverService oauthNaverService;

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    // 네이버 소셜로그인을 통한 유저 정보 반환
    public UserDto oauth2AuthorizationNaver(String code, String state) {
        AuthorizationNaverDto authorizationNaverDto = oauthNaverService.callTokenApi(code, state);

        // 소셜로그인 정보
        String userInfoFromNaver = oauthNaverService.callGetUserByAccessToken(authorizationNaverDto.getAccess_token());

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);
        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = jsonResponseInfo.get("id").toString();

        log.info(userSocialAccountId);

        return socialAccountRepository.findById(userSocialAccountId)
                // socialAccount가 이미 등록되어 있는 경우, 기존 정보를 담은 userDto(USER) 반환
                .map( socialAccount -> {
                    User user = socialAccount.getUser();
                    UserDto userDto = new UserDto(user);
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));

                    return userDto;
                })
                // socialAccount가 등록되어 있지 않은 경우, userDto(ANONUMOUS) 반환
                .orElseGet( () -> {
                    UserDto userDto = new UserDto();
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setRole(UserRole.ANONYMOUS);

                    return userDto;
                });
    }

    // 카카오로 인증받기
    public UserDto oauth2AuthorizationKakao(String code) {

        AuthorizationKakaoDto authorizationKakaoDto = oauthKakaoService.callTokenApi(code);

        // 소셜로그인 정보
        String userInfoFromKakao = oauthKakaoService.callGetUserByAccessToken(authorizationKakaoDto.getAccess_token());

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromKakao);
        String userSocialAccountId = jsonUserInfo.get("id").toString();

        // 회원 로그인, 비회원 로그인 체크
        return socialAccountRepository.findById(userSocialAccountId)
                .map(socialAccount -> {
                    User user = socialAccount.getUser();
                    UserDto userDto = new UserDto(user);
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));

                    return userDto;
                })
                .orElseGet(() -> {
                    UserDto userDto = new UserDto();
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setRole(UserRole.ANONYMOUS);

                    return userDto;
                });
    }

    // 회원가입
    public UserDto signUp(UserSignUpRequest userSignUpRequest) {

        System.out.println(userSignUpRequest);

        SocialAccount socialAccount = socialAccountRepository.save(
                new SocialAccount(
                        userSignUpRequest.getSocialAccountId(),
                        userSignUpRequest.getSocialAccountType()
                )
        );

        System.out.println(userSignUpRequest);

        User user = userRepository.save(
                new User(userSignUpRequest.getNickName(),
                        userSignUpRequest.getEmail(),
                        userSignUpRequest.getPhoneNumber())
        );
        user.addSocialAccount(socialAccount);

        String jwt = jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId());

        UserDto userDto = new UserDto(user);
        userDto.setSocialAccountId(userSignUpRequest.getSocialAccountId());
        userDto.setJwt(jwt);
        return userDto;
    }

    // jwt 검증을 통한 유저 정보 반환 및 토큰 재발급 로직
    public Long checkLoginStatus(String jwt) {

        Map<String, Object> payloads = new HashMap<>();
        try {
            payloads = jwtService.getPayloads(jwt);
        } catch (JwtException e) {
            // 비밀키 상이(SignatureException), 토큰 정보 위조(MalformedJwtException) , 만료된 경우(ExpiredJwtException)
            log.debug("There is a problem of getting payloads from jwt.", e.getMessage());
            return -1L;
        } catch (Exception e) {
            log.debug("Unexpected error getting payloads from jwt", e.getMessage());
            return -1L;
        }

        Long userId = Long.valueOf((Integer) payloads.get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));

        if (userRepository.existsById(userId)) return userId;
        else return -1L;
    }

    public Optional<User> find(Long user_id) {
        return userRepository.findById(user_id);
    }

    // 유저 전체 리스트 조회
    public List<UserDto> findAll(){
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
    }
}

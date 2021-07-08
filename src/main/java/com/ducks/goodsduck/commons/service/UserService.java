package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.entity.SocialAccount;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.SocialAccountRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String TOKEN_USAGE = "For member-checking";

    private final OauthNaverService oauthNaverService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtService jwtService;

    // 네이버 소셜로그인을 통한 유저 정보 반환
    @Transactional
    public UserDto oauth2AuthorizationNaver(String code, String state) {
        AuthorizationNaverDto authorizationNaverDto = oauthNaverService.callTokenApi(code, state);

        // 소셜로그인 정보
        String userInfoFromNaver = oauthNaverService.callGetUserByAccessToken(authorizationNaverDto.getAccess_token());

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);
        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = (String) jsonResponseInfo.get("id");

        log.info(userSocialAccountId);

        return socialAccountRepository.findById(userSocialAccountId)
                // socialAccount가 이미 등록되어 있는 경우, 기존 정보를 담은 userDto(USER) 반환
                .map( socialAccount -> {
                    User user = socialAccount.getUser();
                    UserDto userDto = new UserDto(user);
                    userDto.registerJwt(
                            jwtService.createToken(TOKEN_USAGE, new JwtDto(user.getId()))
                    );
                    return userDto.registerSocialAccountId(userSocialAccountId);
                })
                // socialAccount가 등록되어 있지 않은 경우, userDto(ANONUMOUS) 반환
                .orElseGet( () -> UserDto.createUserDto(UserRole.ANONYMOUS).registerSocialAccountId(userSocialAccountId));
    }

    // 회원가입

    public UserDto signUp(UserSignUpRequest userSignUpRequest) {

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

        UserDto userDto = new UserDto(user);
        return userDto.registerJwt(
                jwtService.createToken(TOKEN_USAGE, new JwtDto(savedUser.getId()))
        );
    }

    /** jwt 검증을 통한 유저 정보 반환 및 토큰 재발급 로직 */
    public UserDto checkLoginStatus(String token) {
        Map<String, Object> payloads = new HashMap<>();
        try {
            payloads = jwtService.getPayloads(token);
        } catch (JwtException e) {
            // 비밀키 상이(SignatureException), 토큰 정보 위조(MalformedJwtException) , 만료된 경우(ExpiredJwtException)
            log.warn(e.getMessage());
            return UserDto.createUserDto(UserRole.ANONYMOUS);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return UserDto.createUserDto(UserRole.ANONYMOUS);
        }

        // 토큰의 만료 기한이 다 된 경우
        Long userId = Long.valueOf((Integer) payloads.get("userId"));
//         long userId = payloads.get("userId");

        return userRepository.findById(userId)
                .map(user -> user.login()) // lastLoginAt 갱신
                .map(user -> new UserDto(user))
                .map(userDto -> userDto.registerJwt(
                        // 토큰 재발급
                        jwtService.createToken(TOKEN_USAGE, new JwtDto(userId))
                ))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS));
    }

    public UserDto find(Long user_id) {
        return userRepository.findById(user_id)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)); // user를 못찾으면 빈 UserDto(UserRole.ANONYMOUS) 반환
    }

    // 유저 전체 리스트 조회
    public List<UserDto> findAll(){
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
    }
}

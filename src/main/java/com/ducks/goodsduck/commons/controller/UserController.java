package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Api(tags = "회원 가입 APIs")
public class UserController {

    private final UserService userService;

    @NoCheckJwt
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API")
    @GetMapping("/users/login/naver")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return OK(userService.oauth2AuthorizationNaver(code, state));
    }

    @NoCheckJwt
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 API")
    @GetMapping("/users/login/kakao")
    public ApiResult<UserDto> authorizeKakao(@RequestParam("code") String code) {
        return OK(userService.oauth2AuthorizationKakao(code));
    }

    @NoCheckJwt
    @ApiOperation("회원가입 API")
    @PostMapping("/users/sign-up")
    public ApiResult<UserDto> signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return OK(userService.signUp(userSignUpRequest));
    }

    @ApiOperation("프로필 사진 업로드 API")
    @PutMapping("/users/profile-image")
    public ApiResult<Long> uploadProfileImage(@RequestParam MultipartFile multipartFile, HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.uploadProfileImage(userId, multipartFile));
    }

    @ApiOperation("jwt를 통한 유저 정보 조회 API")
    @GetMapping("/users/look-up")
    @Transactional
    public ApiResult<UserDto> getUser(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        return OK(userService.find(userId)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)));
    }

    @NoCheckJwt
    @ApiOperation("jwt를 통한 유저 ID 조회 API")
    @GetMapping("/users/look-up-id")
    public ApiResult<Long> getUserIdByJwt(@RequestHeader("jwt") String jwt) {
        return OK(userService.checkLoginStatus(jwt));
    }

    @NoCheckJwt
    @GetMapping("/users")
    @ApiOperation("(개발용) 모든 유저 정보 조회 API")
    public ApiResult<List<UserDto>> getUserList() {
        return OK(userService.findAll());
    }
}

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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Api(tags = "회원 가입 APIs")
public class UserController {

    private final UserService userService;

    @NoCheckJwt
    @GetMapping("/users/login/naver")
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return OK(userService.oauth2AuthorizationNaver(code, state));
    }

    @NoCheckJwt
    @GetMapping("/users/login/kakao")
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 API")
    public ApiResult<UserDto> authorizeKakao(@RequestParam("code") String code) {
        return OK(userService.oauth2AuthorizationKakao(code));
    }

    @NoCheckJwt
    @PostMapping("/users/sign-up")
    @ApiOperation("회원가입 API")
    public ApiResult<UserDto> signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return OK(userService.signUp(userSignUpRequest));
    }

    @GetMapping("/users/look-up")
    @ApiOperation("특정 유저 정보 조회 API")
    @Transactional
    public ApiResult<UserDto> getUser(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        return OK(userService.find(userId)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)));
    }

    @NoCheckJwt
    @GetMapping("/users")
    @ApiOperation("(개발용) 모든 유저 정보 조회 API")
    public ApiResult<List<UserDto>> getUserList() {
        return OK(userService.findAll());
    }
}

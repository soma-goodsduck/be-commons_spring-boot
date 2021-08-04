package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.dto.user.UpdateProfileRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.Image;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.service.PriceProposeService;
import com.ducks.goodsduck.commons.service.DeviceService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;
import static com.ducks.goodsduck.commons.model.enums.TradeStatus.valueOf;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "회원 가입 및 유저 관련 APIs")
public class UserController {

    private final UserService userService;
    private final PriceProposeService priceProposeService;
    private final ItemService itemService;
    private final DeviceService deviceService;

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @NoCheckJwt
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 API")
    @GetMapping("/v1/users/login/naver")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return OK(userService.oauth2AuthorizationNaver(code, state));
    }

    @NoCheckJwt
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 API")
    @GetMapping("/v1/users/login/kakao")
    public ApiResult<UserDto> authorizeKakao(@RequestParam("code") String code) {
        return OK(userService.oauth2AuthorizationKakao(code));
    }

    @NoCheckJwt
    @ApiOperation("회원가입 API")
    @PostMapping("/v1/users/sign-up")
    public ApiResult<UserDto> signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return OK(userService.signUp(userSignUpRequest));
    }

    @ApiOperation("프로필 통합 수정 API")
    @PutMapping("/v1/users/profile")
    public ApiResult<Boolean> updateProfile(@RequestParam(required = false) MultipartFile multipartFile,
                                            @RequestParam String stringProfileDto,
                                            HttpServletRequest request) throws Exception {

        UpdateProfileRequest updateProfileRequest = new ObjectMapper().readValue(stringProfileDto, UpdateProfileRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateProfile(userId, multipartFile, updateProfileRequest));
    }

    @NoCheckJwt
    @ApiOperation("닉네임 중복 확인 API")
    @PostMapping("/v1/users/nickname-check")
    public ApiResult<Boolean> checkSameNickname(@RequestBody NicknameRequest nicknameRequest) {
        return OK(userService.checkNickname(nicknameRequest.getNickName()));
    }

    @ApiOperation(value = "좋아하는 아이돌 편집 API", notes = "좋아하는 아이돌 그룹이 추가/삭제될 경우, 기존에 있었던 아이돌 그룹 포함 List형태로 요청")
    @PutMapping("/v1/users/idol-groups")
    public ApiResult<Long> updateLikeIdolGroups(@RequestBody UserIdolGroupUpdateRequest userIdolGroupUpdateRequest,
                                                HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateLikeIdolGroups(userId, userIdolGroupUpdateRequest.getLikeIdolGroupsId()));
    }

    @ApiOperation("프로필 사진 업로드 API")
    @PutMapping("/v1/users/profile-image")
    public ApiResult<Long> uploadProfileImage(@RequestParam(required = false) MultipartFile multipartFile,
                                              HttpServletRequest request) throws IOException {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.uploadProfileImage(userId, multipartFile));
    }

    @ApiOperation("유저 닉네임 수정 API")
    @PutMapping("/v1/users/nickname")
    public ApiResult<Long> updateNickname(@RequestBody NicknameRequest nicknameRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateNickname(userId, nicknameRequest.getNickName()));
    }

    @ApiOperation("jwt를 통한 유저 정보 조회 API")
    @GetMapping("/v1/users/look-up")
    @Transactional
    public ApiResult<UserDto> getUser(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.find(userId)
                .map(user -> new UserDto(user))
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)));
    }

    @ApiOperation("jwt를 통한 유저 ID 조회 API")
    @GetMapping("/v1/users/look-up-id")
    @Transactional
    public ApiResult<UserSimpleDto> getUserIdByJwt(HttpServletRequest request) throws Exception {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userRepository.findById(userId).map(user -> new UserSimpleDto(user))
                .orElseThrow(() -> new Exception("not find user id")));
    }

    @ApiOperation(value = "마이페이지의 아이템 거래내역 불러오기 API")
    @GetMapping("/v1/users/items")
    public ApiResult<List<ItemSummaryDto>> getMyItemList(HttpServletRequest request, @RequestParam("tradeStatus") String tradeStatus) throws Exception {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        try {
            TradeStatus status = valueOf(tradeStatus.toUpperCase());
            return OK(itemService.findMyItem(userId, status)
                    .stream()
                    .map(tuple -> {
                        Item item = tuple.get(0, Item.class);
                        ImageDto imageDto = Optional.ofNullable(tuple.get(1, Image.class))
                                .map(image -> new ImageDto(image))
                                .orElseGet(() -> new ImageDto());
                        return ItemSummaryDto.of(item, imageDto);
                    })
                    .collect(Collectors.toList()));

        } catch (IllegalArgumentException e) {
            log.debug("Exception occurred in parsing tradeStatus: {}", e.getMessage(), e);
            throw new IllegalArgumentException("There is no tradeStatus inserted");
        } catch (NullPointerException e) {
            log.debug("Exception during parsing from tuple: {}", e.getMessage(), e);
            throw new NullPointerException(e.getMessage());
        } catch (Exception e) {
            throw new Exception("Unexpected exception occurred.");
        }
    }

    @GetMapping("/v1/users/items/price-propose")
    @ApiOperation(value = "특정 유저가 받은 가격 제안 요청 목록 보기 API", notes = "SUGGESTED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllProposeToMe(HttpServletRequest request) {
        var userId = (Long)request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllReceiveProposeByUser(userId));
    }

    @GetMapping("/v1/users/price-propose")
    @ApiOperation(value = "요청한 가격 제안 목록 보기 API", notes = "SUGGESTED, REFUSED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllProposeFromMe(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllGiveProposeByUser(userId));
    }

    @PostMapping("/v1/users/device")
    @ApiOperation("(FCM) 사용자 디바이스의 Registration Token을 등록하는 API")
    public ApiResult registerDevice(HttpServletRequest request, @RequestHeader("registrationToken") String registrationToken) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        Device savedDevice = deviceService.registerFCMToken(userId, registrationToken);
        return OK(
                new DeviceResponse(savedDevice.getUuid())
        );
    }

    // TODO : 개발중 (경원)
    @NoCheckJwt
    @ApiOperation(value = "다른 사람의 프로필 보기 (ver.bcryptId)")
    @GetMapping("/v1/users/{bcryptUserId}")
    @Transactional
    public ApiResult<OtherUserPageDto> getItemsOfUser(@PathVariable("bcryptUserId") String bcryptId) {
        return OK(userService.showOtherUserPage(bcryptId));
    }

    @NoCheckJwt
    @GetMapping("/v1/users")
    @ApiOperation("(개발용) 모든 유저 정보 조회 API")
    public ApiResult<List<UserDto>> getUserList() {
        return OK(userService.findAll());
    }
}

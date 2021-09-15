package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.exception.user.InvalidJwtException;
import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.dto.checksame.EmailCheckRequest;
import com.ducks.goodsduck.commons.model.dto.checksame.NicknameCheckRequest;
import com.ducks.goodsduck.commons.model.dto.checksame.PhoneNumberCheckRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemSummaryDto;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationResponse;
import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.dto.review.TradeCompleteReponse;
import com.ducks.goodsduck.commons.model.dto.sms.SmsAuthenticationRequest;
import com.ducks.goodsduck.commons.model.dto.sms.SmsTransmitRequest;
import com.ducks.goodsduck.commons.model.dto.user.*;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationRedisResponse;
import com.ducks.goodsduck.commons.service.*;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;
import static com.ducks.goodsduck.commons.model.enums.TradeStatus.valueOf;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "유저 관련 APIs")
public class UserController {

    private final UserService userService;
    private final PriceProposeService priceProposeService;
    private final ItemService itemService;
    private final DeviceService deviceService;
    private final UserChatService userChatService;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final SmsAuthenticationService smsAuthenticationService;
    private final AddressService addressService;
    private final AccountService accountService;

    @NoCheckJwt
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 with 인가코드 API")
    @GetMapping("/v1/users/login/naver")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code,
                                             @RequestParam("state") String state,
                                             @RequestParam("clientId") String clientId) {
        return OK(userService.oauth2AuthorizationNaver(code, state, clientId));
    }

    @NoCheckJwt
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 with 인가코드 API")
    @GetMapping("/v1/users/login/kakao")
    public ApiResult<UserDto> authorizeKakao(@RequestParam("code") String code) {
        log.debug("Request code of Kakao's login: " + code);
        return OK(userService.oauth2AuthorizationKakao(code));
    }

    @NoCheckJwt
    @ApiOperation("소셜로그인_APPLE 토큰 발급 및 사용자 정보 조회 with 인가코드 API")
    @GetMapping("/v1/users/login/apple")
    public ApiResult<UserDto> authorizeApple(@RequestParam("state") String state,
                                             @RequestParam("code") String code,
                                             @RequestParam("idToken") String idToken) {
        log.debug("Request of Apple's login: \n\tstate: {}, code: {}, idToken: {}", state, code, idToken);
        return OK(userService.oauth2AuthorizationApple(state, code, idToken));
    }

    @NoCheckJwt
    @ApiOperation("회원가입 API")
    @PostMapping("/v1/users/sign-up")
    public ApiResult<UserDto> signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return OK(userService.signUp(userSignUpRequest));
    }

    @ApiOperation(value = "회원 탈퇴 API", notes = "사용자 권한을 RESIGNED로 수정함")
    @PatchMapping("/v1/users")
    public ApiResult resign(HttpServletRequest request, @RequestBody UserPhoneNumberRequest userPhoneNumberRequest) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.resign(userId, userPhoneNumberRequest));
    }

    @NoCheckJwt
    @ApiOperation("전화번호 중복 확인 API")
    @PostMapping("/v1/users/phone-number-check")
    public ApiResult<SocialType> checkSamePhoneNumber(@RequestBody PhoneNumberCheckRequest phoneNumberCheckRequest) {
        return OK(userService.checkPhoneNumber(phoneNumberCheckRequest.getPhoneNumber()));
    }

    @NoCheckJwt
    @ApiOperation("닉네임 중복 확인 API")
    @PostMapping("/v1/users/nickname-check")
    public ApiResult<Boolean> checkSameNickname(@RequestBody NicknameCheckRequest nicknameCheckRequest) {
        return OK(userService.checkNickname(nicknameCheckRequest.getNickName()));
    }

    @NoCheckJwt
    @ApiOperation("이메일 중복 확인 API")
    @PostMapping("/v1/users/email-check")
    public ApiResult<Boolean> checkSameEmail(@RequestBody EmailCheckRequest emailCheckRequest) {
        return OK(userService.checkEmail(emailCheckRequest.getEmail()));
    }

    @ApiOperation("프로필 통합 수정 API")
    @PutMapping("/v1/users/profile")
    public ApiResult<Boolean> updateProfile(@RequestParam String stringProfileDto,
                                            @RequestParam(required = false) MultipartFile multipartFile,
                                            HttpServletRequest request) throws Exception {

        UpdateProfileRequest updateProfileRequest = new ObjectMapper().readValue(stringProfileDto, UpdateProfileRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateProfile(userId, multipartFile, updateProfileRequest));
    }

    @ApiOperation(value = "좋아하는 아이돌 수정 API", notes = "좋아하는 아이돌 그룹이 추가/삭제될 경우, 기존에 있었던 아이돌 그룹 포함 List형태로 요청")
    @PutMapping("/v1/users/idol-groups")
    public ApiResult<Long> updateLikeIdolGroups(@RequestBody UserIdolGroupUpdateRequest userIdolGroupUpdateRequest,
                                                HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateLikeIdolGroups(userId, userIdolGroupUpdateRequest.getLikeIdolGroupsId()));
    }

    //TODO: jwt를 클라이언트에서 사용하고 있는지 확인 필요
    @NoCheckJwt
    @ApiOperation("jwt를 통한 유저 정보 조회 API")
    @GetMapping("/v1/users/look-up")
    @Transactional
    public ApiResult<UserDto> getUser(@RequestHeader("jwt") String jwt, HttpServletResponse response) {

        Long userId = userService.checkLoginStatus(jwt);
        if(userId.equals(-1L)) {
            throw new InvalidJwtException();
        }

        String newJwt = jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, userId);
        response.setHeader("jwt", newJwt);
        return OK(userService.findUserInfoByUserId(userId, newJwt));
    }

    @ApiOperation("jwt를 통한 유저 ID 조회 API")
    @GetMapping("/v1/users/look-up-id")
    @Transactional
    public ApiResult<UserSimpleDto> getUserIdByJwt(HttpServletRequest request, HttpServletResponse response) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.findByUserId(userId));
    }
    
    @ApiOperation(value = "마이 페이지 + 아이템 거래내역 불러오기 API")
    @GetMapping("/v1/users/items")
    public ApiResult<MypageResponse> getMyItemList(HttpServletRequest request, @RequestParam("tradeStatus") String tradeStatus) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        TradeStatus status = valueOf(tradeStatus.toUpperCase());
        return OK(itemService.findMyItem(userId, status));
    }

    @ApiOperation(value = "마이 페이지 아이템 중 거래 미완료 상품 번호 조회 API")
    @GetMapping("/v1/users/items/not-complete")
    public ApiResult<List<Long>> getMyItemNumbersNotCompleted(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(itemService.getMyItemNumbersNotCompleted(userId));
    }

    @NoCheckJwt
    @ApiOperation(value = "다른 유저의 프로필 보기")
    @GetMapping("/v1/users/{bcryptId}")
    public ApiResult<OtherUserPageDto> showOtherUserPage(@PathVariable("bcryptId") String bcryptId) {
        return OK(userService.showOtherUserPage(bcryptId));
    }

    @NoCheckJwt
    @ApiOperation(value = "다른 유저의 프로필 보기에서 전체 상품 보기", notes = "초기 tradeStatus 값은 selling으로 설정")
    @GetMapping("/v1/users/{bcryptId}/items")
    public ApiResult<List<ItemSummaryDto>> getItemsOfOtherUser(@PathVariable("bcryptId") String bcryptId,
                                                               @RequestParam("tradeStatus") String stringTradeStatus) {
        TradeStatus tradeStatus = valueOf(stringTradeStatus.toUpperCase());
        return OK(itemService.getItemsOfOtherUser(bcryptId, tradeStatus));
    }

    @ApiOperation("특정 아이템에 해당하는 채팅방 목록 조회 API (게시물 주인 jwt 필요)")
    @GetMapping("/v1/users/items/{itemId}/chat")
    @Transactional
    public ApiResult<TradeCompleteReponse> getUserChatListByItem(HttpServletRequest request, @PathVariable("itemId") Long itemId) throws IllegalAccessException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        Item tradeCompletedItem = itemService.findById(itemId);
        List<UserChatResponse> userChats = userChatService.findByItemId(userId, itemId);

        return OK(new TradeCompleteReponse(tradeCompletedItem, userChats));
    }

    @ApiOperation("특정 아이템에 해당하는 채팅방 목록 조회 API V2 (게시물 주인 jwt 필요)")
    @GetMapping("/v2/users/items/{itemId}/chat")
    public ApiResult<TradeCompleteReponse> getUserChatListByItemV2(HttpServletRequest request, @PathVariable("itemId") Long itemId) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.findByItemIdV2(userId, itemId));
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
    @ApiOperation("사용자 디바이스의 FCM Registration Token을 등록하는 API (알림 권한 허용 시에도 사용)")
    public ApiResult registerDevice(HttpServletRequest request, @RequestHeader("registrationToken") String registrationToken) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(deviceService.register(userId, registrationToken));
    }

    @DeleteMapping("/v1/users/device")
    @ApiOperation("사용자 디바이스의 알림 여부 차단")
    public ApiResult discardDevice(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        deviceService.discard(userId);
        return OK(true);
    }

    @GetMapping("/v1/users/notifications")
    @ApiOperation("사용자가 받은 알림 목록 조회 API")
    public ApiResult<List<NotificationResponse>> getNotificationsOfUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.getNotificationsOfUserId(userId));
    }

    @GetMapping("/v2/users/notifications")
    @ApiOperation("사용자가 받은 알림 목록 조회 API V2")
    public ApiResult<List<NotificationRedisResponse>> getNotificationsOfUserV2(HttpServletRequest request) throws JsonProcessingException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.getNotificationsOfUserIdV2(userId));
    }

    @NoCheckJwt
    @PostMapping("/v1/sms")
    @ApiOperation("SMS 인증 문자 전송")
    public ApiResult sendAuthenticationSms(@RequestBody SmsTransmitRequest smsTransmitRequest) throws Exception {
        String phoneNumber = smsTransmitRequest.getPhoneNumber();
        return OK(smsAuthenticationService.sendSmsOfAuthentication(phoneNumber));
    }

    @NoCheckJwt
    @PostMapping("/v1/sms/authentication")
    @ApiOperation("SMS 인증 번호에 대한 검증")
    public ApiResult authenticateBySms(@RequestBody SmsAuthenticationRequest smsAuthenticationRequest) {
        String phoneNumber = smsAuthenticationRequest.getPhoneNumber();
        String authenticationNumber = smsAuthenticationRequest.getAuthenticationNumber();
        return OK(smsAuthenticationService.authenticate(phoneNumber, authenticationNumber));
    }

    @GetMapping("/v1/users/address")
    @ApiOperation("배송정보 가져오기 (회원)")
    public ApiResult<AddressDto> getAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.getAddress(userId));
    }

    @PostMapping("/v1/users/address")
    @ApiOperation("배송정보 등록하기 (회원)")
    public ApiResult<Boolean> registerAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.registerAddress(userId, addressDto));
    }

    @PutMapping("/v1/users/address")
    @ApiOperation("배송정보 수정하기 (회원)")
    public ApiResult<Boolean> editAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.editAddress(userId, addressDto));
    }

    @GetMapping("/v1/users/account")
    @ApiOperation("계좌정보 가져오기 (회원)")
    public ApiResult<AccountDto> getAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.getAccount(userId));
    }

    @PostMapping("/v1/users/account")
    @ApiOperation("계좌정보 등록하기 (회원)")
    public ApiResult<Boolean> registerAccount(@RequestBody AccountDto accountDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.registerAccount(userId, accountDto));
    }

    @PutMapping("/v1/users/account")
    @ApiOperation("계좌정보 수정하기 (회원)")
    public ApiResult<Boolean> editAccount(@RequestBody AccountDto accountDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.editAccount(userId, accountDto));
    }

    @NoCheckJwt
    @GetMapping("/v1/users")
    @ApiOperation("(개발용) 모든 유저 정보 조회 API")
    public ApiResult<List<UserDto>> getUserList() {
        return OK(userService.findAll());
    }
}

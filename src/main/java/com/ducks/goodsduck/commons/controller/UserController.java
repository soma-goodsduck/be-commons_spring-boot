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
import com.ducks.goodsduck.commons.model.dto.sms.SmsAuthenticationResponse;
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
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;
import static com.ducks.goodsduck.commons.model.enums.TradeStatus.valueOf;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "?????? ?????? APIs")
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
    @ApiOperation("???????????????_NAVER ?????? ?????? ??? ????????? ?????? ?????? with ???????????? API")
    @GetMapping("/v1/users/login/naver")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code,
                                             @RequestParam("state") String state,
                                             @RequestParam("clientId") String clientId) {
        return OK(userService.oauth2AuthorizationNaver(code, state, clientId));
    }

    @NoCheckJwt
    @ApiOperation("???????????????_KAKAO ?????? ?????? ??? ????????? ?????? ?????? with ???????????? API")
    @GetMapping("/v1/users/login/kakao")
    public ApiResult<UserDto> authorizeKakao(@RequestParam("code") String code) {
        log.debug("Request code of Kakao's login: " + code);
        return OK(userService.oauth2AuthorizationKakao(code));
    }

    @NoCheckJwt
    @ApiOperation("???????????????_APPLE ?????? ?????? ??? ????????? ?????? ?????? with ???????????? API")
    @GetMapping("/v1/users/login/apple")
    public ApiResult<UserDto> authorizeApple(@RequestParam("state") String state,
                                             @RequestParam("code") String code,
                                             @RequestParam("idToken") String idToken) {
        log.debug("Request of Apple's login: \n\tstate: {}, code: {}, idToken: {}", state, code, idToken);
        return OK(userService.oauth2AuthorizationApple(state, code, idToken));
    }

    @NoCheckJwt
    @ApiOperation("?????? ???????????? API")
    @PostMapping("/v1/users/sign-up")
    public ApiResult<UserDto> signUpUser(@RequestBody UserSignUpRequest userSignUpRequest) {
        return OK(userService.signUp(userSignUpRequest));
    }

    @NoCheckJwt
    @ApiOperation("?????? ???????????? API V2 (?????????)")
    @PostMapping("/v2/users/sign-up")
    public ApiResult<UserDtoV2> signUpUserV2(@Valid @RequestBody UserSignUpRequestV2 userSignUpRequest) {
        return OK(userService.signUpV2(userSignUpRequest));
    }

    @NoCheckJwt
    @ApiOperation("?????? ????????? API (?????????)")
    @PostMapping("/v1/users/login")
    public ApiResult<UserDtoV2> login(@RequestBody UserLoginRequest userLoginRequest) {
        return OK(userService.login(userLoginRequest));
    }

    @NoCheckJwt
    @ApiOperation("???????????? ????????? API (?????????)")
    @PostMapping("/v1/users/reset-password")
    public ApiResult<Boolean> resetPassword(@RequestBody UserResetRequest userResetRequest) {
        return OK(userService.resetPassword(userResetRequest));
    }
    
    @ApiOperation("???????????? ????????? API in ??????????????? (??????)")
    @PostMapping("/v1/users/reset-password-member")
    public ApiResult<Boolean> resetPasswordForMember(@RequestBody UserResetRequestForMember userResetRequestForMember) {
        return OK(userService.resetPasswordForMember(userResetRequestForMember));
    }

    @ApiOperation(value = "?????? ?????? API", notes = "????????? ????????? RESIGNED??? ?????????")
    @PatchMapping("/v1/users")
    public ApiResult<Boolean> resign(@RequestBody UserPhoneNumberRequest userPhoneNumberRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.resign(userId, userPhoneNumberRequest));
    }

    @ApiOperation(value = "?????? ?????? API V2")
    @PutMapping("/v2/users")
    public ApiResult<Boolean> resign(@RequestBody UserResignRequest userResignRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.resignV2(userId, userResignRequest.getPassword()));
    }

    @NoCheckJwt
    @ApiOperation("???????????? ?????? ?????? API")
    @PostMapping("/v1/users/phone-number-check")
    public ApiResult<SocialType> checkSamePhoneNumber(@RequestBody PhoneNumberCheckRequest phoneNumberCheckRequest) {
        return OK(userService.checkPhoneNumber(phoneNumberCheckRequest.getPhoneNumber()));
    }

    @NoCheckJwt
    @ApiOperation("???????????? ?????? ?????? API V2")
    @PostMapping("/v2/users/phone-number-check")
    public ApiResult<String> checkSamePhoneNumberV2(@RequestBody PhoneNumberCheckRequest phoneNumberCheckRequest) {
        return OK(userService.checkPhoneNumberV2(phoneNumberCheckRequest.getPhoneNumber()));
    }

    @NoCheckJwt
    @ApiOperation("????????? ?????? ?????? API (?????????, ????????????)")
    @PostMapping("/v1/users/nickname-check-register")
    public ApiResult<Boolean> checkSameNickname(@RequestBody NicknameCheckRequest nicknameCheckRequest) {
        return OK(userService.checkNicknameRegister(nicknameCheckRequest.getNickName()));
    }

    @ApiOperation("????????? ?????? ?????? API (??????)")
    @PostMapping("/v1/users/nickname-check")
    public ApiResult<CheckNicknameDto> checkSameNickname(@RequestBody NicknameCheckRequest nicknameCheckRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.checkNickname(userId, nicknameCheckRequest.getNickName()));
    }

    @NoCheckJwt
    @ApiOperation("????????? ?????? ?????? API")
    @PostMapping("/v1/users/email-check")
    public ApiResult<Boolean> checkSameEmail(@RequestBody EmailCheckRequest emailCheckRequest) {
        return OK(userService.checkEmail(emailCheckRequest.getEmail()));
    }

    @ApiOperation("????????? ?????? ?????? API")
    @PutMapping("/v1/users/profile")
    public ApiResult<Boolean> updateProfile(@RequestParam String stringProfileDto,
                                            @RequestParam(required = false) MultipartFile multipartFile,
                                            HttpServletRequest request) throws IOException {

        UpdateProfileRequest updateProfileRequest = new ObjectMapper().readValue(stringProfileDto, UpdateProfileRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateProfile(userId, multipartFile, updateProfileRequest));
    }

    @ApiOperation(value = "???????????? ????????? ?????? API", notes = "???????????? ????????? ????????? ??????/????????? ??????, ????????? ????????? ????????? ?????? ?????? List????????? ??????")
    @PutMapping("/v1/users/idol-groups")
    public ApiResult<Long> updateLikeIdolGroups(@RequestBody UserIdolGroupUpdateRequest userIdolGroupUpdateRequest,
                                                HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.updateLikeIdolGroups(userId, userIdolGroupUpdateRequest.getLikeIdolGroupsId()));
    }

    @NoCheckJwt
    @ApiOperation("jwt??? ?????? ?????? ?????? ?????? API")
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

    @ApiOperation("jwt??? ?????? ?????? ID ?????? API")
    @GetMapping("/v1/users/look-up-id")
    @Transactional
    public ApiResult<UserSimpleDto> getUserIdByJwt(HttpServletRequest request, HttpServletResponse response) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.findByUserId(userId));
    }
    
    @ApiOperation(value = "?????? ????????? + ????????? ???????????? ???????????? API")
    @GetMapping("/v1/users/items")
    public ApiResult<MypageResponse> getMyItemList(HttpServletRequest request, @RequestParam("tradeStatus") String tradeStatus) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        TradeStatus status = valueOf(tradeStatus.toUpperCase());
        return OK(itemService.findMyItem(userId, status));
    }

    @ApiOperation(value = "?????? ????????? ????????? ??? ?????? ????????? ?????? ?????? ?????? API")
    @GetMapping("/v1/users/items/not-complete")
    public ApiResult<List<Long>> getMyItemNumbersNotCompleted(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(itemService.getMyItemNumbersNotCompleted(userId));
    }

    @NoCheckJwt
    @ApiOperation(value = "?????? ????????? ????????? ??????")
    @GetMapping("/v1/users/{bcryptId}")
    public ApiResult<OtherUserPageDto> showOtherUserPage(@PathVariable("bcryptId") String bcryptId) {
        return OK(userService.showOtherUserPage(bcryptId));
    }

    @NoCheckJwt
    @ApiOperation(value = "?????? ????????? ????????? ???????????? ?????? ?????? ??????", notes = "?????? tradeStatus ?????? selling?????? ??????")
    @GetMapping("/v1/users/{bcryptId}/items")
    public ApiResult<List<ItemSummaryDto>> getItemsOfOtherUser(@PathVariable("bcryptId") String bcryptId,
                                                               @RequestParam("tradeStatus") String stringTradeStatus) {
        TradeStatus tradeStatus = valueOf(stringTradeStatus.toUpperCase());
        return OK(itemService.getItemsOfOtherUser(bcryptId, tradeStatus));
    }

    @ApiOperation("?????? ???????????? ???????????? ????????? ?????? ?????? API (????????? ?????? jwt ??????)")
    @GetMapping("/v1/users/items/{itemId}/chat")
    @Transactional
    public ApiResult<TradeCompleteReponse> getUserChatListByItem(HttpServletRequest request, @PathVariable("itemId") Long itemId) throws IllegalAccessException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        Item tradeCompletedItem = itemService.findById(itemId);
        List<UserChatResponse> userChats = userChatService.findByItemId(userId, itemId);

        return OK(new TradeCompleteReponse(tradeCompletedItem, userChats));
    }

    @ApiOperation("?????? ???????????? ???????????? ????????? ?????? ?????? API V2 (????????? ?????? jwt ??????) - ??? ?????? ??????")
    @GetMapping("/v2/users/items/{itemId}/chat")
    public ApiResult<TradeCompleteReponse> getUserChatListByItemV2(HttpServletRequest request, @PathVariable("itemId") Long itemId) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.findByItemIdV2(userId, itemId));
    }

    @GetMapping("/v1/users/items/price-propose")
    @ApiOperation(value = "?????? ????????? ?????? ?????? ?????? ?????? ?????? ?????? API", notes = "SUGGESTED ????????? ?????? ????????? ??????")
    public ApiResult<List<PriceProposeResponse>> getAllProposeToMe(HttpServletRequest request) {
        var userId = (Long)request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllReceiveProposeByUser(userId));
    }

    @GetMapping("/v1/users/price-propose")
    @ApiOperation(value = "????????? ?????? ?????? ?????? ?????? API", notes = "SUGGESTED, REFUSED ????????? ?????? ????????? ??????")
    public ApiResult<List<PriceProposeResponse>> getAllProposeFromMe(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllGiveProposeByUser(userId));
    }

    @PostMapping("/v1/users/device")
    @ApiOperation("????????? ??????????????? FCM Registration Token??? ???????????? API (?????? ?????? ?????? ????????? ??????)")
    public ApiResult<Boolean> registerDevice(HttpServletRequest request, @RequestHeader("registrationToken") String registrationToken) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(deviceService.register(userId, registrationToken));
    }

    @DeleteMapping("/v1/users/device")
    @ApiOperation("????????? ??????????????? ?????? ?????? ??????")
    public ApiResult<Boolean> discardDevice(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        deviceService.discard(userId);
        return OK(true);
    }

    @GetMapping("/v1/users/notifications")
    @ApiOperation("???????????? ?????? ?????? ?????? ?????? API")
    public ApiResult<List<NotificationResponse>> getNotificationsOfUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.getNotificationsOfUserId(userId));
    }

    @GetMapping("/v2/users/notifications")
    @ApiOperation("???????????? ?????? ?????? ?????? ?????? API V2")
    public ApiResult<List<NotificationRedisResponse>> getNotificationsOfUserV2(HttpServletRequest request) throws JsonProcessingException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.getNotificationsOfUserIdV2(userId));
    }

    @DeleteMapping("/v1/users/notifications")
    @ApiOperation("???????????? ?????? ?????? ?????? ????????? API")
    public ApiResult<Boolean> cleanOfNotificationsOfUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.cleanOfNotificationsOfUser(userId));
    }

    @NoCheckJwt
    @PostMapping("/v1/sms")
    @ApiOperation("SMS ?????? ?????? ??????")
    public ApiResult<Boolean> sendAuthenticationSms(@RequestBody SmsTransmitRequest smsTransmitRequest) {
        String phoneNumber = smsTransmitRequest.getPhoneNumber();
        return OK(smsAuthenticationService.sendSmsOfAuthentication(phoneNumber));
    }

    @NoCheckJwt
    @PostMapping("/v1/sms/authentication")
    @ApiOperation("SMS ?????? ????????? ?????? ??????")
    public ApiResult<Boolean> authenticateBySms(@RequestBody SmsAuthenticationRequest smsAuthenticationRequest) {
        String phoneNumber = smsAuthenticationRequest.getPhoneNumber();
        String authenticationNumber = smsAuthenticationRequest.getAuthenticationNumber();
        return OK(smsAuthenticationService.authenticate(phoneNumber, authenticationNumber));
    }

    @NoCheckJwt
    @PostMapping("/v1/sms/authentication-find")
    @ApiOperation("SMS ?????? ????????? ?????? ?????? for ?????????/???????????? ?????? (?????????)")
    public ApiResult<SmsAuthenticationResponse> authenticateBySmsForFind(@RequestBody SmsAuthenticationRequest smsAuthenticationRequest) {
        String phoneNumber = smsAuthenticationRequest.getPhoneNumber();
        String authenticationNumber = smsAuthenticationRequest.getAuthenticationNumber();
        return OK(smsAuthenticationService.authenticateForFind(phoneNumber, authenticationNumber));
    }

    @GetMapping("/v1/users/address")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<AddressDto> getAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.getAddress(userId));
    }

    @PostMapping("/v1/users/address")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<Boolean> registerAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.registerAddress(userId, addressDto));
    }

    @PutMapping("/v1/users/address")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<Boolean> editAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(addressService.editAddress(userId, addressDto));
    }

    @GetMapping("/v1/users/account")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<AccountDto> getAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.getAccount(userId));
    }

    @PostMapping("/v1/users/account")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<Boolean> registerAccount(@RequestBody AccountDto accountDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.registerAccount(userId, accountDto));
    }

    @PutMapping("/v1/users/account")
    @ApiOperation("???????????? ???????????? (??????)")
    public ApiResult<Boolean> editAccount(@RequestBody AccountDto accountDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(accountService.editAccount(userId, accountDto));
    }

    @GetMapping("/v1/users/vote")
    @ApiOperation(value = "????????? ?????? ?????? ?????? ?????? API", notes = "????????? ????????? ????????? ????????? ??????, ?????? ????????? ????????? ??????/?????? ??????")
    public ApiResult<UserVoteResponse> getVoteInfoByUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.findVoteInfoByUser(userId));
    }

    // ?????? ?????? ?????? API
    @PostMapping("/v1/users/blocked-users/{bcryptId}")
    @ApiOperation(value = "?????? ?????? ?????? API", notes = "?????? ????????? ???????????? ?????? ???????????? ?????????.")
    public ApiResult<Boolean> addBlockedUser(HttpServletRequest request, @PathVariable("bcryptId") String bcryptId) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.addBlockedUser(userId, bcryptId));
    }

    @NoCheckJwt
    @GetMapping("/v1/users")
    @ApiOperation("(?????????) ?????? ?????? ?????? ?????? API")
    public ApiResult<List<UserDto>> getUserList() {
        return OK(userService.findAll());
    }
}

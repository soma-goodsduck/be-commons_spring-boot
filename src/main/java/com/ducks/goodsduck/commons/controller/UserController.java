package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.dto.checkSame.EmailCheckRequest;
import com.ducks.goodsduck.commons.model.dto.checkSame.NicknameCheckRequest;
import com.ducks.goodsduck.commons.model.dto.checkSame.PhoneNumberCheckRequest;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationResponse;
import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.dto.review.TradeCompleteReponse;
import com.ducks.goodsduck.commons.model.dto.user.*;
import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.*;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.NoResultException;
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

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @NoCheckJwt
    @ApiOperation("소셜로그인_NAVER 토큰 발급 및 사용자 정보 조회 with 인가코드 API")
    @GetMapping("/v1/users/login/naver")
    public ApiResult<UserDto> authorizeNaver(@RequestParam("code") String code, @RequestParam("state") String state) {
        return OK(userService.oauth2AuthorizationNaver(code, state));
    }

    @NoCheckJwt
    @ApiOperation("소셜로그인_KAKAO 토큰 발급 및 사용자 정보 조회 with 인가코드 API")
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

//    @NoCheckJwt
//    @ApiOperation("전화번호 중복 확인 API")
//    @PostMapping("/v1/users/phone-number-check")
//    public ApiResult<UserDto> checkSamePhoneNumber(@RequestBody PhoneNumberCheckRequest phoneNumberCheckRequest) {
//        return OK(userService.checkPhoneNumber(phoneNumberCheckRequest.getPhoneNumber()));
//    }

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

    // TODO : 삭제 예정 (FE 사용 X)
//    @ApiOperation("(삭제 예정) 프로필 사진 업로드 API")
//    @PutMapping("/v1/users/profile-image")
//    public ApiResult<Long> uploadProfileImage(@RequestParam(required = false) MultipartFile multipartFile,
//                                              HttpServletRequest request) throws IOException {
//
//        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
//        return OK(userService.uploadProfileImage(userId, multipartFile));
//    }

    // TODO : 삭제 예정 (FE 사용 X)
//    @ApiOperation("(삭제 예정) 유저 닉네임 수정 API")
//    @PutMapping("/v1/users/nickname")
//    public ApiResult<Long> updateNickname(@RequestBody NicknameRequest nicknameRequest, HttpServletRequest request) {
//        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
//        return OK(userService.updateNickname(userId, nicknameRequest.getNickName()));
//    }

    @NoCheckJwt
    @ApiOperation("jwt를 통한 유저 정보 조회 API")
    @GetMapping("/v1/users/look-up")
    @Transactional
    public ApiResult<UserDto> getUser(@RequestHeader("jwt") String jwt, HttpServletResponse response) {

        Long userId = userService.checkLoginStatus(jwt);
        String newJwt = jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, userId);

        response.setHeader("jwt", newJwt);

        return OK(userService.find(userId)
                .map(user -> {
                    UserDto userDto = new UserDto(user);
                    userDto.setJwt(newJwt);
                    return userDto;
                })
                .orElseGet(() -> UserDto.createUserDto(UserRole.ANONYMOUS)));
    }

    @ApiOperation("jwt를 통한 유저 ID 조회 API")
    @GetMapping("/v1/users/look-up-id")
    @Transactional
    public ApiResult<UserSimpleDto> getUserIdByJwt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userRepository.findById(userId).map(user -> new UserSimpleDto(user))
                .orElseThrow(() -> new Exception("not find user id")));
    }
    
    @ApiOperation(value = "마이페이지의 아이템 거래내역 불러오기 API")
    @GetMapping("/v1/users/items")
    public ApiResult<MypageResponse> getMyItemList(HttpServletRequest request, @RequestParam("tradeStatus") String tradeStatus) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        TradeStatus status = valueOf(tradeStatus.toUpperCase());
        return OK(itemService.findMyItem(userId, status));
    }

    @ApiOperation("특정 아이템에 해당하는 채팅방 목록 조회 API (게시물 주인 jwt 필요)")
    @GetMapping("/v1/users/items/{itemId}/chat")
    @Transactional
    public ApiResult<TradeCompleteReponse> getUserChatListByItem(HttpServletRequest request, @PathVariable("itemId") Long itemId) throws IllegalAccessException {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        Item tradeCompletedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    throw new NoResultException("Item not founded.");
                });

        List<UserChatResponse> userChats = userChatService.findByItemId(userId, itemId);

        return OK(new TradeCompleteReponse(tradeCompletedItem, userChats));
    }

    @ApiOperation("특정 아이템에 해당하는 채팅방 목록 조회 API V2 (게시물 주인 jwt 필요)")
    @GetMapping("/v2/users/items/{itemId}/chat")
    public ApiResult<TradeCompleteReponse> getUserChatListByItemV2(HttpServletRequest request, @PathVariable("itemId") Long itemId) throws IllegalAccessException {
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
    @ApiOperation("(FCM) 사용자 디바이스의 Registration Token을 등록하는 API")
    public ApiResult registerDevice(HttpServletRequest request, @RequestHeader("registrationToken") String registrationToken) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        Device savedDevice = deviceService.registerFCMToken(userId, registrationToken);
        return OK(
                new DeviceResponse(savedDevice.getUuid())
        );
    }

    @GetMapping("/v1/users/notifications")
    @ApiOperation("사용자가 받은 알림 목록 조회 API")
    public ApiResult<List<NotificationResponse>> getNotificationsOfUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.getNotificationsOfUserId(userId));
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

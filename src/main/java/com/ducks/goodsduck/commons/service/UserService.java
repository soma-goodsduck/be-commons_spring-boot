package com.ducks.goodsduck.commons.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.model.dto.OtherUserPageDto;
import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationKakaoDto;
import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationNaverDto;
import com.ducks.goodsduck.commons.model.dto.user.UpdateProfileRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final CustomJwtService jwtService;
    private final OauthKakaoService oauthKakaoService;
    private final OauthNaverService oauthNaverService;
    private final ImageUploadService imageUploadService;

    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final ItemRepository itemRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final UserIdolGroupRepository userIdolGroupRepository;
    private final UserItemRepository userItemRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final UserImageRepository userImageRepository;

    // 네이버 소셜로그인을 통한 유저 정보 반환
    public UserDto oauth2AuthorizationNaver(String code, String state) throws ParseException {

        AuthorizationNaverDto authorizationNaverDto = oauthNaverService.callAccessToken(code, state);

        // 소셜로그인 정보
        String userInfoFromNaver = oauthNaverService.callUserInfoByAccessToken(authorizationNaverDto.getAccess_token());

        // 비회원 체크
//        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(userInfoFromNaver);
        JSONObject jsonUserInfo = (org.json.simple.JSONObject) obj;

        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = jsonResponseInfo.get("id").toString();

        // COMMENT: 네이버에서 받아오는 String 값 확인 (모바일에서 문제)
        log.debug(userInfoFromNaver);

        return socialAccountRepository.findById(userSocialAccountId)
                // socialAccount가 이미 등록되어 있는 경우, 기존 정보를 담은 userDto(USER) 반환
                .map(socialAccount -> {
                    User user = socialAccount.getUser();
                    UserDto userDto = new UserDto(user);
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));

                    return userDto;
                })
                // socialAccount가 등록되어 있지 않은 경우, userDto(ANONUMOUS) 반환
                .orElseGet(() -> {
                    UserDto userDto = new UserDto();
                    userDto.setSocialAccountId(userSocialAccountId);
                    userDto.setRole(UserRole.ANONYMOUS);

                    return userDto;
                });
    }

    // 카카오로 인증받기
    public UserDto oauth2AuthorizationKakao(String code) throws ParseException {

        AuthorizationKakaoDto authorizationKakaoDto = oauthKakaoService.callAccessToken(code);

        // 소셜로그인 정보
        String userInfoFromKakao = oauthKakaoService.callUserInfoByAccessToken(authorizationKakaoDto.getAccess_token());

        // 비회원 체크
//        JSONObject jsonUserInfo = new JSONObject(userInfoFromKakao);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(userInfoFromKakao);
        JSONObject jsonUserInfo = (org.json.simple.JSONObject) obj;

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

        if(userSignUpRequest.getPhoneNumber() == null || userSignUpRequest.getEmail() == null ||
                userSignUpRequest.getNickName() == null || userSignUpRequest.getLikeIdolGroupsId() == null) {
            throw new IllegalStateException("No sing-up info in UserController.singUp");
        }

        SocialAccount socialAccount = socialAccountRepository.save(
                new SocialAccount(userSignUpRequest.getSocialAccountId(),
                                  userSignUpRequest.getSocialAccountType())
        );

        User user = userRepository.save(
                new User(userSignUpRequest.getNickName(),
                         userSignUpRequest.getEmail(),
                         userSignUpRequest.getPhoneNumber())
        );
        user.addSocialAccount(socialAccount);

        List<Long> likeIdolGroupsId = userSignUpRequest.getLikeIdolGroupsId();
        for (Long likeIdolGroupId : likeIdolGroupsId) {

            IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId).get();
            UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
            user.addUserIdolGroup(userIdolGroup);
            userIdolGroupRepository.save(userIdolGroup);
        }

        String jwt = jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId());

        UserDto userDto = new UserDto(user);
        userDto.setSocialType(userSignUpRequest.getSocialAccountType());
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
            log.info("There is a problem of getting payloads from jwt.", e.getMessage());
            return -1L;
        } catch (Exception e) {
            log.info("Unexpected error getting payloads from jwt (no member)", e.getMessage());
            return -1L;
        }

        Long userId = Long.valueOf((Integer) payloads.get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));

        if (userRepository.existsById(userId)) return userId;
        else return -1L;
    }

    public Boolean updateProfile(Long userId, MultipartFile multipartFile, UpdateProfileRequest updateProfileRequest) throws Exception {

        try {
            User user = userRepository.findById(userId).get();

            // 프로필 사진 수정
            if (multipartFile != null) {

                // 현재 프로필 사진이 있으면 삭제
                if (user.getImageUrl() != null) {
                    Image nowImage = imageRepository.findByUrl(user.getImageUrl());
                    imageRepository.delete(nowImage);
                }

                Image image = imageUploadService.uploadImage(multipartFile, ImageType.PROFILE, user.getNickName());
                ProfileImage profileImage = new ProfileImage(image);
                profileImage.setUser(user);

                user.setImageUrl(image.getUrl());
                imageRepository.save(profileImage);
            }

            // 닉네임 수정
            user.setNickName(updateProfileRequest.getNickName());

            // 좋아하는 아이돌 수정
            List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();
            userIdolGroupRepository.deleteInBatch(userIdolGroups);
            userIdolGroups.clear();

            List<Long> likeIdolGroupsId = updateProfileRequest.getLikeIdolGroupsId();
            for (Long likeIdolGroupId : likeIdolGroupsId) {
                IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId).get();
                UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
                user.addUserIdolGroup(userIdolGroup);
            }
            userIdolGroupRepository.saveAll(userIdolGroups);

            return true;
        } catch (Exception e) {
            throw new Exception("Fail to edit profile");
        }
    }

    // TODO : 추후 삭제 예정 (FE 사용X)
//    public Long uploadProfileImage(Long userId, MultipartFile multipartFile) throws IOException {
//
//        try {
//            Image image = imageUploadService.uploadImage(multipartFile, ImageType.PROFILE);
//
//            User user = userRepository.findById(userId).get();
//            user.setImageUrl(image.getUrl());
//
//            return userId;
//        } catch (Exception e) {
//            return -1L;
//        }
//    }

    // TODO : 추후 삭제 예정 (FE 사용X)
//    public Long updateNickname(Long userId, String newNickname) {
//        User user = userRepository.findById(userId).get();
//
//        try {
//            user.setNickName(newNickname);
//            return userId;
//        } catch (Exception e) {
//            return -1L;
//        }
//    }

    public Long updateLikeIdolGroups(Long userId, List<Long> likeIdolGroupsId) {

        User user = userRepository.findById(userId).get();

        try {
            List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();
            userIdolGroupRepository.deleteInBatch(userIdolGroups);
            userIdolGroups.clear();

            for (Long likeIdolGroupId : likeIdolGroupsId) {
                IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId).get();
                UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
                user.addUserIdolGroup(userIdolGroup);
            }
            userIdolGroupRepository.saveAll(userIdolGroups);

            return userId;
        } catch (Exception e) {
            return -1L;
        }
    }

    public String uploadChatImage(MultipartFile multipartFile, ImageType imageType, Long userId) throws IOException, ImageProcessingException, MetadataException {
        User user = userRepository.findById(userId).get();
        System.out.println(user);

        return imageUploadService.uploadImage(multipartFile, imageType, user.getNickName()).getUrl();
    }

    public void updateLastLoginAt(Long userId) {

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
    }

    public Optional<User> find(Long userId) {
        return userRepository.findById(userId);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());
    }

    public SocialType checkPhoneNumber(String phoneNumber) {

        User user = userRepository.findByPhoneNumber(phoneNumber);

        if(user != null) {
            SocialAccount socialAccount = socialAccountRepository.findByUserId(user.getId());
            return socialAccount.getType();
        } else {
            return null;
        }
    }

//    public UserDto checkPhoneNumber(String phoneNumber) {
//
//        User user = userRepository.findByPhoneNumber(phoneNumber);
//
//        if(user != null) {
//            SocialAccount socialAccount = socialAccountRepository.findByUserId(user.getId());
//            UserDto userDto = new UserDto(user);
//            userDto.setSocialType(socialAccount.getType());
//            userDto.setSocialAccountId(socialAccount.getId());
//            return userDto;
//        } else {
//            return UserDto.createUserDto(UserRole.ANONYMOUS);
//        }
//    }

    public Boolean checkNickname(String nickname) {

        User user = userRepository.findByNickName(nickname);
        if(user != null) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean checkEmail(String email) {

        User user = userRepository.findByEmail(email);

        if(user != null) {
            return false;
        } else {
            return true;
        }
    }


    public OtherUserPageDto showOtherUserPage(String bcryptId) {

        User user = userRepository.findByBcryptId(bcryptId);
        Long userId = user.getId();

        // 판매상품
        List<Item> items = user.getItems();
        List<Item> showItems = new ArrayList<>();
        List<Item> sortedItems = items.stream()
                .sorted((item1, item2) -> item2.getId().compareTo(item1.getId()))
                .collect(Collectors.toList());

        if (sortedItems.size() > 3) {
            showItems.add(sortedItems.get(0));
            showItems.add(sortedItems.get(1));
            showItems.add(sortedItems.get(2));
        } else {
            showItems = sortedItems;
        }

        // 후기
        List<Review> reviews = reviewRepositoryCustom.findByReveiverId(userId);

        // 판매상품, 후기, 보증스탬프 개수
        Integer itemCount = items.size();
        Long reviewCount = reviewRepositoryCustom.countByReveiverId(userId);

        return new OtherUserPageDto(user, itemCount, reviewCount, showItems, reviews);
    }
}

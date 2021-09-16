package com.ducks.goodsduck.commons.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.dto.CheckNicknameDto;
import com.ducks.goodsduck.commons.model.dto.OtherUserPageDto;
import com.ducks.goodsduck.commons.model.dto.oauth2.*;
import com.ducks.goodsduck.commons.model.dto.user.UpdateProfileRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.dto.user.UserPhoneNumberRequest;
import com.ducks.goodsduck.commons.model.dto.user.UserSignUpRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ProfileImage;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.review.ReviewRepository;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.util.OauthAppleLoginUtil;
import com.ducks.goodsduck.commons.util.OauthKakaoLoginUtil;
import com.ducks.goodsduck.commons.util.OauthNaverLoginUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.UserRole.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final CustomJwtService jwtService;
    private final ImageUploadService imageUploadService;
    private MessageSource messageSource;

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
    private final DeviceRepository deviceRepository;

    // 네이버 소셜로그인을 통한 유저 정보 반환
    public UserDto oauth2AuthorizationNaver(String code, String state, String clientId) {

        AuthorizationNaverDto authorizationNaverDto = OauthNaverLoginUtil.callAccessToken(code, state, clientId);

        log.debug("Social login user's DTO: \n" + authorizationNaverDto);

        // 소셜로그인 정보
        String userInfoFromNaver = OauthNaverLoginUtil.callUserInfoByAccessToken(authorizationNaverDto.getAccess_token());

        log.debug("Now login user information: \n" + userInfoFromNaver);

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);

        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = jsonResponseInfo.get("id").toString();

        // COMMENT: 네이버에서 받아오는 String 값 확인 (모바일에서 문제)
        log.debug(userInfoFromNaver);

        return checkUserAndGetInfo(userSocialAccountId);
    }

    // 카카오로 인증받기
    public UserDto oauth2AuthorizationKakao(String code) {

        AuthorizationKakaoDto authorizationKakaoDto = OauthKakaoLoginUtil.callAccessToken(code);

        // 소셜로그인 정보
        String userInfoFromKakao = OauthKakaoLoginUtil.callUserInfoByAccessToken(authorizationKakaoDto.getAccess_token());

        log.debug("Now login user information: \n" + userInfoFromKakao);

        // 비회원 체크
        JSONObject jsonUserInfo = new JSONObject(userInfoFromKakao);
        String userSocialAccountId = jsonUserInfo.get("id").toString();

        // 회원 로그인, 비회원 로그인 체크
        return checkUserAndGetInfo(userSocialAccountId);
    }

    public UserDto oauth2AuthorizationApple(String state, String code, String idToken) {

        // Apple 서버 - 소셜 로그인 정보 요청 및 응답
        Claims claims = getClaimsBy(idToken);
        log.debug("Information of User from Apple Server: {}", claims);
        String subOfUser = claims.get("sub").toString();

        return checkUserAndGetInfo(subOfUser);
    }

    public Claims getClaimsBy(String idToken) {
        try {
            Map<String, Object> headers = jwtService.getHeaderWithoutSignedKey(idToken);
            String kidOfIdToken = (String) headers.get("kid");
            List<PublicKeyOfApple> publicKeyOfApples = OauthAppleLoginUtil.callPublicToken();
            PublicKeyOfApple realPublicKey = null;
            for (PublicKeyOfApple publicKeyOfApple : publicKeyOfApples) {
                if (kidOfIdToken.equals(publicKeyOfApple.getKid())) {
                    realPublicKey = publicKeyOfApple;
                    break;
                }
            }

            if (realPublicKey == null) {
                throw new RuntimeException("Can't get information from Apple server.");
            }

            byte[] nBytes = Base64.getUrlDecoder().decode(realPublicKey.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(realPublicKey.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance(realPublicKey.getKty());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(idToken).getBody();
        } catch (NoSuchAlgorithmException e) {
            log.info("", e);
            return null;
        } catch (InvalidKeySpecException e) {
            log.info("", e);
            return null;
        } catch (JsonProcessingException e) {
            log.info("", e);
            return null;
        }
    }

    // 회원 로그인, 비회원 로그인 체크
    public UserDto checkUserAndGetInfo(String userInfoFromOauth2) {
        return socialAccountRepository.findById(userInfoFromOauth2)
                .map(socialAccount -> {
                    User user = socialAccount.getUser();
                    Device device = deviceRepository.findByUser(user)
                            .orElseGet(() -> new Device(user));
                    deviceRepository.save(device);
                    UserDto userDto = new UserDto(user);
                    userDto.setSocialAccountId(userInfoFromOauth2);
                    userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));
                    userDto.setAgreeToNotification(device.getIsAllowed());

                    return userDto;
                })
                .orElseGet(() -> {
                    UserDto userDto = new UserDto();
                    userDto.setSocialAccountId(userInfoFromOauth2);
                    userDto.setRole(ANONYMOUS);

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
        user.setImageUrl(PropertyUtil.BASIC_IMAGE_URL);

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

        log.debug("user's jwt is : " + jwt);

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
            if(!user.getNickName().equals(updateProfileRequest.getNickName())) {
                user.setUpdatedAt(LocalDateTime.now());
            }
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

    public CheckNicknameDto checkNickname(Long userId, String nickname) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
        User findUser = userRepository.findByNickName(nickname);

        if(findUser == null) {
            return new CheckNicknameDto(user.getUpdatedAt(), false);
        } else {
            return new CheckNicknameDto(user.getUpdatedAt(), true);
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
        List<Item> items = user.getItems()
                .stream()
                .filter(item -> item.getDeletedAt() != null)
                .collect(Collectors.toList());
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

    public Boolean resign(Long userId, UserPhoneNumberRequest userPhoneNumberRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new NoResultException("User not founded.");
                });

        if (!user.getPhoneNumber().equals(userPhoneNumberRequest.getPhoneNumber())) {
            return false;
        }

        return userRepositoryCustom.updateRoleByUserId(userId, RESIGNED) > 0 ? true : false;
    }
}

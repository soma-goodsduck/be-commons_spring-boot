package com.ducks.goodsduck.commons.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.exception.common.DuplicatedDataException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.dto.CheckNicknameDto;
import com.ducks.goodsduck.commons.exception.common.InvalidRequestDataException;
import com.ducks.goodsduck.commons.exception.image.ImageProcessException;
import com.ducks.goodsduck.commons.exception.image.InvalidMetadataException;
import com.ducks.goodsduck.commons.exception.user.Oauth2Exception;
import com.ducks.goodsduck.commons.model.dto.OtherUserPageDto;
import com.ducks.goodsduck.commons.model.dto.oauth2.*;
import com.ducks.goodsduck.commons.model.dto.user.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ProfileImage;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.model.enums.SocialType;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.device.DeviceRepository;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.user.UserRepositoryCustom;
import com.ducks.goodsduck.commons.repository.useridolgroup.UserIdolGroupRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
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

import static com.ducks.goodsduck.commons.model.enums.SocialType.APPLE;
import static com.ducks.goodsduck.commons.model.enums.UserRole.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final CustomJwtService jwtService;
    private final ImageUploadService imageUploadService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final SocialAccountRepository socialAccountRepository;
    private final IdolGroupRepository idolGroupRepository;
    private final UserIdolGroupRepository userIdolGroupRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final DeviceRepository deviceRepository;
    private final MessageSource messageSource;

    // ????????? ?????????????????? ?????? ?????? ?????? ??????
    public UserDto oauth2AuthorizationNaver(String code, String state, String clientId) {

        AuthorizationNaverDto authorizationNaverDto = OauthNaverLoginUtil.callAccessToken(code, state, clientId);

        log.debug("Social login user's DTO: \n" + authorizationNaverDto);

        // ??????????????? ??????
        String userInfoFromNaver = OauthNaverLoginUtil.callUserInfoByAccessToken(authorizationNaverDto.getAccess_token());

        // COMMENT: ??????????????? ???????????? String ??? ?????? (??????????????? ??????)
        log.debug("Now login user information: \n" + userInfoFromNaver);

        // ????????? ??????
        JSONObject jsonUserInfo = new JSONObject(userInfoFromNaver);

        JSONObject jsonResponseInfo = (JSONObject) jsonUserInfo.get("response");
        String userSocialAccountId = jsonResponseInfo.get("id").toString();

        return checkUserAndGetInfo(userSocialAccountId);
    }

    // ???????????? ????????????
    public UserDto oauth2AuthorizationKakao(String code) {

        AuthorizationKakaoDto authorizationKakaoDto = OauthKakaoLoginUtil.callAccessToken(code);

        // ??????????????? ??????
        String userInfoFromKakao = OauthKakaoLoginUtil.callUserInfoByAccessToken(authorizationKakaoDto.getAccess_token());

        log.debug("Now login user information: \n" + userInfoFromKakao);

        // ????????? ??????
        JSONObject jsonUserInfo = new JSONObject(userInfoFromKakao);
        String userSocialAccountId = jsonUserInfo.get("id").toString();

        // ?????? ?????????, ????????? ????????? ??????
        return checkUserAndGetInfo(userSocialAccountId);
    }

    public UserDto oauth2AuthorizationApple(String state, String code, String idToken) {

        // Apple ?????? - ?????? ????????? ?????? ?????? ??? ??????
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
                throw new Oauth2Exception(APPLE);
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

    // ?????? ?????????, ????????? ????????? ??????
    public UserDto checkUserAndGetInfo(String userInfoFromOauth2) {
        return socialAccountRepository.findById(userInfoFromOauth2)
                .map(socialAccount -> {
                    User user = socialAccount.getUser();
                    UserDto userDto = new UserDto(user);
                    userDto.setSocialAccountId(userInfoFromOauth2);
                    userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));

                    return userDto;
                })
                .orElseGet(() -> {
                    UserDto userDto = new UserDto();
                    userDto.setSocialAccountId(userInfoFromOauth2);
                    userDto.setRole(ANONYMOUS);

                    return userDto;
                });
    }

    // ????????????
    public UserDto signUp(UserSignUpRequest userSignUpRequest) {

        if(userSignUpRequest.getPhoneNumber() == "" || userSignUpRequest.getEmail() == "" || userSignUpRequest.getNickName() == "") {
            throw new InvalidRequestDataException();
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

        em.flush();
        em.close();

        return userDto;
    }

    // ???????????? V2
    public UserDtoV2 signUpV2(UserSignUpRequestV2 userSignUpRequest) {

        String nickName = userSignUpRequest.getNickName();
        String email = userSignUpRequest.getEmail();
        String phoneNumber = userSignUpRequest.getPhoneNumber();

        // COMMENT: ????????? ?????? (?????????, ????????? ??????, ?????????)
        if (userRepository.existsByEmail(email) || userRepository.existsByPhoneNumber(phoneNumber) || userRepository.existsByNickName(nickName))
            throw new InvalidRequestDataException("There is duplicated data in input data.");

        User user = userRepository.save(
                new User(nickName,
                         email,
                         phoneNumber)
        );
        user.setImageUrl(PropertyUtil.BASIC_IMAGE_URL);
        String encodedPassword = passwordEncoder.encode(userSignUpRequest.getPassword());
        user.setPassword(encodedPassword);
        user.setMarketingAgree(userSignUpRequest.getMarketingAgree());

        List<Long> likeIdolGroupsId = userSignUpRequest.getLikeIdolGroupsId();
        for (Long likeIdolGroupId : likeIdolGroupsId) {

            IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId).get();
            UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
            user.addUserIdolGroup(userIdolGroup);
            userIdolGroupRepository.save(userIdolGroup);
        }

        String jwt = jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId());
        UserDtoV2 userDto = new UserDtoV2(user);
        userDto.setJwt(jwt);
        return userDto;
    }

    // jwt ????????? ?????? ?????? ?????? ?????? ??? ?????? ????????? ??????
    public Long checkLoginStatus(String jwt) {

        Map<String, Object> payloads = new HashMap<>();

        log.debug("user's jwt is : " + jwt);

        try {
            payloads = jwtService.getPayloads(jwt);
        } catch (JwtException e) {
            // ????????? ??????(SignatureException), ?????? ?????? ??????(MalformedJwtException) , ????????? ??????(ExpiredJwtException)
            log.info("There is a problem of getting payloads from jwt.", e.getMessage());
            return -1L;
        } catch (Exception e) {
            log.info("Unexpected error getting payloads from jwt (no member)", e.getMessage());
            return -1L;
        }

        Long userId = Long.valueOf((Integer) payloads.get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return -1L;
        else {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            if (!user.getHaveGetGrantOfAttend()) {
                user.grantOfAttend();
                user.setHaveGetGrantOfAttend(true);
            }
            return user.getId();
        }
    }

    public Boolean updateProfile(Long userId, MultipartFile multipartFile, UpdateProfileRequest updateProfileRequest) throws IOException {

        User user = userRepository.findById(userId).get();

        // ????????? ?????? ??????
        if (multipartFile != null) {

            // ?????? ???????????? ?????? ?????? X
            if(!user.getImageUrl().equals(PropertyUtil.BASIC_IMAGE_URL)) {
                Image nowImage = imageRepository.findByUrl(user.getImageUrl());
//                imageUploadService.deleteImage(nowImage, ImageType.PROFILE);
                imageRepository.delete(nowImage);
            }

            Image image = null;
            try {
                image = imageUploadService.uploadImage(multipartFile, ImageType.PROFILE, user.getNickName());
            } catch (ImageProcessingException e) {
                log.debug("Exception occured during processing ItemImage: {}", e.getMessage(), e.getStackTrace());
                throw new ImageProcessException();
            } catch (MetadataException e) {
                log.debug("Exception occured during reading Metadata of Item: {}", e.getMessage(), e.getStackTrace());
                throw new InvalidMetadataException();
            }
            ProfileImage profileImage = new ProfileImage(image);
            profileImage.setUser(user);

            user.setImageUrl(image.getUrl());
            imageRepository.save(profileImage);
        }

        // ????????? ??????
        if(!user.getNickName().equals(updateProfileRequest.getNickName())) {
            user.setUpdatedAt(LocalDateTime.now());
        }
        user.setNickName(updateProfileRequest.getNickName());

        // ???????????? ????????? ??????
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();
        userIdolGroupRepository.deleteInBatch(userIdolGroups);
        userIdolGroups.clear(); // ??????

        List<Long> likeIdolGroupsId = updateProfileRequest.getLikeIdolGroupsId();
        for (Long likeIdolGroupId : likeIdolGroupsId) {
            IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId).get();
            UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
            user.addUserIdolGroup(userIdolGroup);
        }
        userIdolGroupRepository.saveAll(userIdolGroups);

        return true;
    }

    public Long updateLikeIdolGroups(Long userId, List<Long> likeIdolGroupsId) {

        User user = userRepository.findById(userId).get();

        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();
        userIdolGroupRepository.deleteInBatch(userIdolGroups);
        userIdolGroups.clear();

        for (Long likeIdolGroupId : likeIdolGroupsId) {
            IdolGroup likeIdolGroup = idolGroupRepository.findById(likeIdolGroupId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"IdolGroup"}, null)));
            UserIdolGroup userIdolGroup = UserIdolGroup.createUserIdolGroup(likeIdolGroup);
            user.addUserIdolGroup(userIdolGroup);
        }
        userIdolGroupRepository.saveAll(userIdolGroups);

        return userId;
    }

    public String uploadChatImage(MultipartFile multipartFile, ImageType imageType, Long userId) throws IOException {
        User user = userRepository.findById(userId).get();

        try {
            return imageUploadService.uploadImage(multipartFile, imageType, user.getNickName()).getUrl();
        } catch (ImageProcessingException e) {
            log.debug("Exception occured during processing ItemImage: {}", e.getMessage(), e.getStackTrace());
            throw new ImageProcessException();
        } catch (MetadataException e) {
            log.debug("Exception occured during reading Metadata of Item: {}", e.getMessage(), e.getStackTrace());
            throw new InvalidMetadataException();
        }
    }

    public void updateLastLoginAt(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
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

    public String checkPhoneNumberV2(String phoneNumber) {

        User user = userRepository.findByPhoneNumber(phoneNumber);
        if(user != null && user.getDeletedAt() != null) {
            return "RESIGNED";
        } else if(user != null) {
            return "GOODSDUCK";
        } else {
            return null;
        }
    }

    public Boolean checkNicknameRegister(String nickname) {

        User user = userRepository.findByNickName(nickname);
        if(user != null) {
            return false;
        } else {
            return true;
        }
    }

    public CheckNicknameDto checkNickname(Long userId, String nickname) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
        User findUser = userRepository.findByNickName(nickname);
        
        if(findUser == null) {
            return new CheckNicknameDto(user.getUpdatedAt(), false, user.getNickName());
        }
        // ?????? ????????? ?????? ???????????? ??????
        else {
            // ?????? ?????? ???????????? ????????? ?????? ???????????? X
            if(user.getNickName().equals(nickname)) {
                return new CheckNicknameDto(user.getUpdatedAt(), false, user.getNickName());
            } else {
                return new CheckNicknameDto(user.getUpdatedAt(), true, user.getNickName());
            }
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

        // ????????????
        List<Item> items = user.getItems()
                .stream()
                .filter(item -> item.getDeletedAt() == null)
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

        // ??????
        List<Review> reviews = reviewRepositoryCustom.findByReveiverId(userId);

        // ????????????, ??????, ??????????????? ??????
        Integer itemCount = items.size();
        Long reviewCount = reviewRepositoryCustom.countByReveiverId(userId);

        return new OtherUserPageDto(user, itemCount, reviewCount, showItems, reviews);
    }

    public UserDtoV2 login(UserLoginRequest userLoginRequest) {

        User user = userRepository.findByEmail(userLoginRequest.getEmail());
        if(user == null) {
            UserDtoV2 userDto = new UserDtoV2();
            userDto.setEmailSuccess(false);
            return userDto;
        }

        if(!(passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword()))) {
            UserDtoV2 userDto = new UserDtoV2();
            userDto.setEmailSuccess(true);
            userDto.setPasswordSuccess(false);
            return userDto;
        }

        UserDtoV2 userDto = new UserDtoV2(user);
        userDto.setEmailSuccess(true);
        userDto.setPasswordSuccess(true);
        userDto.setJwt(jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, user.getId()));
        return userDto;
    }

    public Boolean resetPassword(UserResetRequest userResetRequest)  {

        User user = userRepository.findByEmail(userResetRequest.getEmail());
        if(user == null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"User"}, null));
        }

        String encodedPassword = passwordEncoder.encode(userResetRequest.getPassword());
        user.setPassword(encodedPassword);
        return true;
    }

    public Boolean resetPasswordForMember(UserResetRequestForMember userResetRequestForMember) {

        User user = userRepository.findByEmail(userResetRequestForMember.getEmail());
        if(user == null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"Item"}, null));
        }

        if(!(passwordEncoder.matches(userResetRequestForMember.getNowPassword(), user.getPassword()))) {
            return false;
        }

        String encodedPassword = passwordEncoder.encode(userResetRequestForMember.getNewPassword());
        user.setPassword(encodedPassword);
        return true;
    }

    public Boolean resign(Long userId, UserPhoneNumberRequest userPhoneNumberRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        if (!user.getPhoneNumber().equals(userPhoneNumberRequest.getPhoneNumber())) {
            return false;
        }

        return userRepositoryCustom.updateRoleByUserId(userId, RESIGNED) > 0 ? true : false;
    }

    public Boolean resignV2(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        if(!(passwordEncoder.matches(password, user.getPassword()))) {
            return false;
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setRole(RESIGNED);
        return true;
    }

    public UserSimpleDto findByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserSimpleDto(user))
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
    }

    public UserDto findUserInfoByUserId(Long userId, String jwt) {
        User user = userRepository.findById(userId).get();

        Device device = deviceRepository.findByUser(user)
                .orElseGet(() -> new Device(user));
        UserDto userDto = new UserDto(user);
        userDto.setJwt(jwt);
        userDto.setAgreeToNotification(device.getIsAllowed());

        return userDto;
    }

    public UserVoteResponse findVoteInfoByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        Long votedIdolGroupId = user.getVotedIdolGroupId();
        if (votedIdolGroupId == null) return new UserVoteResponse(0L);
        return new UserVoteResponse(votedIdolGroupId);
    }

    public Boolean addBlockedUser(Long userId, String bcryptId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        User blockedUser = userRepository.findByBcryptId(bcryptId);
        List<Long> blockedUserIds = user.getBlockedUserIds();
        if (blockedUserIds.contains(blockedUser.getId())) throw new DuplicatedDataException("Already exists.");
        blockedUserIds.add(blockedUser.getId());
        em.flush();
        em.clear();
        return true;
    }

    public Boolean addBlockedItem(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        List<Long> blockedItemIds = user.getBlockedItemIds();
        if (blockedItemIds.contains(itemId)) throw new DuplicatedDataException("Already exists.");
        blockedItemIds.add(itemId);
        em.flush();
        em.clear();
        return true;
    }

    public Boolean addBlockedPost(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        List<Long> blockedPostIds = user.getBlockedPostIds();
        if (blockedPostIds.contains(postId)) throw new DuplicatedDataException("Already exists.");
        blockedPostIds.add(postId);
        em.flush();
        em.clear();
        return true;
    }
}

package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.user.UnauthorizedException;
import com.ducks.goodsduck.commons.model.dto.chat.ChatMessageRequest;
import com.ducks.goodsduck.commons.model.dto.notification.*;
import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.enums.ActivityType;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
import com.ducks.goodsduck.commons.model.redis.ChatRedis;
import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import com.ducks.goodsduck.commons.repository.chat.ChatRedisTemplate;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.notification.NotificationRedisTemplate;
import com.ducks.goodsduck.commons.repository.notification.NotificationRepository;
import com.ducks.goodsduck.commons.repository.notification.NotificationRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepositoryCustom;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepositoryCustomImpl;
import com.ducks.goodsduck.commons.util.FcmUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.*;
import static com.ducks.goodsduck.commons.model.enums.UserRole.ADMIN;
import static com.google.firebase.messaging.Notification.*;

@Service
@Transactional
@Slf4j
public class NotificationService {

    private final DeviceRepositoryCustom deviceRepositoryCustom;
    private final NotificationRepository notificationRepository;
    private final NotificationRepositoryCustomImpl notificationRepositoryCustomImpl;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final UserRepository userRepository;
    private final NotificationRedisTemplate notificationRedisTemplate;
    private final ChatRedisTemplate chatRedisTemplate;
    private final MessageSource messageSource;

    private final ObjectMapper objectMapper;

    static final String DOMAIN_ADDRESS = "https://goods-duck.com/";
    static final String ANDROID_CLICK_ACTION = "android.intent.action.MAIN";
    static final String NOTIFICATION_TITLE = "GOODSDUCK";
    static final String NOTIFICATION_ICON_URI = "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/sample_goodsduck.png";

    public NotificationService(DeviceRepositoryCustomImpl userDeviceRepositoryCustom,
                               NotificationRepository notificationRepository,
                               NotificationRepositoryCustomImpl notificationRepositoryCustomImpl,
                               UserChatRepositoryCustomImpl userChatRepositoryCustom,
                               UserRepository userRepository,
                               NotificationRedisTemplate notificationRedisTemplate,
                               ChatRedisTemplate chatRedisTemplate, MessageSource messageSource, ObjectMapper objectMapper) {
        this.deviceRepositoryCustom = userDeviceRepositoryCustom;
        this.notificationRepository = notificationRepository;
        this.notificationRepositoryCustomImpl = notificationRepositoryCustomImpl;
        this.userChatRepositoryCustom = userChatRepositoryCustom;
        this.userRepository = userRepository;
        this.notificationRedisTemplate = notificationRedisTemplate;
        this.chatRedisTemplate = chatRedisTemplate;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    /**
     * PricePropose ??? ?????? ?????????
     * Redis??? ?????? + FCM ??????
     * @param senderId
     * @param priceProposeResponse
     * @throws JsonProcessingException
     */
    public void sendMessageOfPricePropose(Long senderId, PriceProposeResponse priceProposeResponse) throws JsonProcessingException {

        User receiver = userRepository.findById(priceProposeResponse.getReceiverId())
            .orElseThrow(() -> {
                throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null));
            });

        Notification notification = new Notification(receiver, priceProposeResponse);

        NotificationRedis notificationRedis = new NotificationRedis(notification.getPriceProposeId(),
                notification.getPrice(),
                notification.getItemId(),
                notification.getItemName(),
                notification.getSenderNickName(),
                notification.getSenderImageUrl());

        saveNotificationAndRequestCloudMessaging(notification.getUser(), notification, notificationRedis, receiver.getId());
    }

    /**
     * Review ???(REVIEW_FIRST, REVIEW) ?????? ?????????
     * Redis??? ?????? + FCM ??????
     * @param reviewType
     * @param savedReview
     * @throws JsonProcessingException
     */
    public void sendMessageOfReview(NotificationType reviewType, Review savedReview) throws JsonProcessingException {

        User sender = savedReview.getUser();

        User receiver = userRepository.findById(savedReview.getReceiverId()).orElseThrow(() -> {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"User"}, null));
        });

        if (sender.gainExpByType(ActivityType.REVIEW) >= 100){
            if (sender.getLevel() == null) sender.setLevel(1);
            boolean success = sender.levelUp();
            if(success) {
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(sender.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }
        }


        Notification notification = new Notification(savedReview, receiver, reviewType);

        NotificationRedis notificationRedis = new NotificationRedis(reviewType,
                notification.getReviewId(),
                notification.getItemId(),
                notification.getItemName(),
                notification.getSenderNickName(),
                notification.getSenderImageUrl());

        saveNotificationAndRequestCloudMessaging(receiver, notification, notificationRedis, receiver.getId());

    }

    private void saveNotificationAndRequestCloudMessaging(User receiver, Notification notification, NotificationRedis notificationRedis, Long receiverId) throws JsonProcessingException {
        notificationRedisTemplate.saveNotificationKeyAndValueByUserId(receiver.getId(), notificationRedis);

        try {
            // ???????????? ????????? Device(FCM ??????) ??????
            List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensByUserId(receiverId);

            if (registrationTokens.isEmpty()) {
                log.debug("Device for notification not founded.");
                return;
            }

            // HINT: ?????? Message ??????
            MulticastMessage message = getMulticastMessage(notification, registrationTokens)
                    .build();
            log.debug("firebase message is : " + message);

            // HINT: ????????????????????? Cloud Messaging ??????
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug(e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
        } catch (Exception e) {
            log.debug(e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
        }
    }

    public void sendMessageOfChat(Long userId, NotificationRequest notificationRequest) throws IOException, IllegalAccessException {

        if (!userId.equals(notificationRequest.getSenderId())) {
            throw new UnauthorizedException("Login user is not matched with senderId.");
        }

        Notification notification;

        try {
            List<UserChat> userChats = userChatRepositoryCustom.findAllByChatId(notificationRequest.getChatRoomId())
                    .stream()
                    // HINT: SENDER??? ???????????? ROW ??????
                    .filter(userChat -> !userChat.getUser().getId().equals(notificationRequest.getSenderId()))
                    .collect(Collectors.toList());

            if (userChats.isEmpty()) {
                throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"UserChat"}, null));
            } else if (userChats.size() > 1) {
                log.debug("UserChats exist total: " + userChats.size());
            }

            var userChat = userChats.get(0);
            var receiver = userChat.getUser();
            var sender = userRepository.findById(notificationRequest.getSenderId())
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                                new Object[]{"User"}, null)));

            notification = new Notification(receiver, sender.getNickName(), sender.getImageUrl(), userChat.getItem().getName(), CHAT);

            notificationRepository.save(notification);

            List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensByUserId(receiver.getId());

            // HINT: ?????? Message ??????
            MulticastMessage message = getMulticastMessage(notification, registrationTokens)
                    .putData("chatRoomId", notificationRequest.getChatRoomId())
                    .build();

            log.debug("firebase message is : " + message);

            // HINT: ????????????????????? Cloud Messaging ??????
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e);
        } catch (Exception e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e);
        }
    }

    public void sendMessageOfChatV2(Long userId, ChatMessageRequest chatMessageRequest) throws IOException, IllegalAccessException, FirebaseMessagingException {

        if (!userId.equals(chatMessageRequest.getSenderId())) {
            throw new UnauthorizedException("Login user is not matched with senderId.");
        }

        List<UserChat> userChats = userChatRepositoryCustom.findAllByChatId(chatMessageRequest.getChatRoomId())
                .stream()
                // HINT: SENDER??? ???????????? ROW ??????
                .filter(userChat -> !userChat.getUser().getId().equals(chatMessageRequest.getSenderId()))
                .collect(Collectors.toList());

        if (userChats.isEmpty()) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"UserChat"}, null));
        } else if (userChats.size() > 1) {
            log.debug("UserChats exist total: " + userChats.size());
        }

        var userChat = userChats.get(0);
        var receiver = userChat.getUser();
        User sender = userRepository.findById(chatMessageRequest.getSenderId())
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        Notification notification = new Notification(receiver, sender.getNickName(), sender.getImageUrl(), userChat.getItem().getName(), CHAT);
        List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensByUserId(receiver.getId());

        try {
            // HINT: ?????? Message ??????
            MulticastMessage message = getMulticastMessage(notification, registrationTokens)
                    .putData("chatRoomId", chatMessageRequest.getChatRoomId())
                    .build();

            log.debug("FCM message is : " + message.toString());

            // HINT: ????????????????????? Cloud Messaging ??????
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
        } catch (Exception e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
        }

        ChatRedis chatRedis = new ChatRedis(chatMessageRequest.getChatMessageId(), chatMessageRequest.getChatRoomId(), chatMessageRequest.getContent(), sender.getNickName());

        chatRedisTemplate.saveChatKeyAndValueByUserId(receiver.getId(), chatRedis);
    }

    private void requestCloudMessagingToFirebase(List<String> registrationTokens, MulticastMessage message) throws FirebaseMessagingException {
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        log.debug("batch response of firebase is :" + response.toString());
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (var i = 0; i < responses.size(); i++) {
                log.debug("firebase responses is: " + responses.get(i));
                if (!responses.get(i).isSuccessful()) {
                    // The order of responses corresponds to the order of the registration tokens.
                    failedTokens.add(registrationTokens.get(i));
                }
            }
            log.debug("List of tokens that caused failures: " + failedTokens);
        }
        log.debug(String.format("Completed successful messaging count: %d", response.getSuccessCount()));
    }

    private MulticastMessage.Builder getMulticastMessage(Notification notification, List<String> registrationTokens) {
        var notificationResponse = new NotificationResponse(notification);
        var notificationMessage = notificationResponse.getMessage();

        return MulticastMessage.builder()
                .setNotification(builder()
                        .setTitle(NOTIFICATION_TITLE)
                        .setBody(notificationMessage.getMessageBody())
                        .setImage(notification.getSenderImageUrl())
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setTitle(NOTIFICATION_TITLE)
                                .setColor("#ffce00")
                                .setBody(notificationMessage.getMessageBody())
                                .setIcon("ic_notification")
                                .setClickAction(ANDROID_CLICK_ACTION)
                                .setImage(notification.getSenderImageUrl())
                                .build())
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setIcon(notificationMessage.getIconUri())
                                .build())
                        .setFcmOptions(WebpushFcmOptions.builder()
                                .setLink(notificationMessage.getMessageUri())
                                .build())
                        .build())
                .addAllTokens(registrationTokens)
                .putData("type", notificationMessage.getType().toString())
                .putData("clickAction", notificationMessage.getMessageUri());
    }

    public List<NotificationResponse> getNotificationsOfUserId(Long userId) {
        return notificationRepositoryCustomImpl.findByUserIdExceptChat(userId)
                .stream()
                .map(notification -> {
                    NotificationResponse notificationResponse = new NotificationResponse(notification);
                    notification.read();
                    return notificationResponse;
                })
                .collect(Collectors.toList());
    }

    public List<NotificationRedisResponse> getNotificationsOfUserIdV2(Long userId) throws JsonProcessingException {

        List<NotificationRedis> notificationRedisList = notificationRedisTemplate.findByUserId(userId);

        List<NotificationRedisResponse> notificationRedisResponses = notificationRedisList
                .stream()
                .map(notificationRedis -> new NotificationRedisResponse(notificationRedis))
                .collect(Collectors.toList());

        int size = notificationRedisList.size()-1;
        while ( size >= 0 && notificationRedisList.get(size).getExpiredAt().isBefore(LocalDateTime.now())) {
            notificationRedisTemplate.rightPopByUserId(userId);
            size--;
        }

        NotificationRedis notificationRedis;
        String stringAsNotificationRedis;
        for (int i = 0; i <= size; i++) {
            notificationRedis = notificationRedisList.get(i);
            if (notificationRedis.getIsRead()) break;
            notificationRedis.read();
            stringAsNotificationRedis = objectMapper.writeValueAsString(notificationRedis);
            notificationRedisTemplate.setKeyAndValueWithIndexByUserId(userId, i, stringAsNotificationRedis);
        }

        return notificationRedisResponses;
    }

    /** ?????? ?????? ?????? ?????? ?????? (From MySQL) */
    public NotificationBadgeResponse checkNewNotification(Long userId) {
        NotificationBadgeResponse notificationBadgeResponse = new NotificationBadgeResponse();
        if (!notificationRepository.existsByUserIdAndTypeNotAndIsReadFalse(userId, CHAT)) notificationBadgeResponse.setHasNewNotification(false);
        if (!notificationRepository.existsByUserIdAndTypeIsAndIsReadFalse(userId, CHAT)) notificationBadgeResponse.setHasNewChat(false);

        return notificationBadgeResponse;
    }

    /** ?????? ?????? ?????? ?????? ?????? (From Redis) */
    public NotificationBadgeResponse checkNewNotificationV2(Long userId) throws JsonProcessingException {
        // HINT: hasNewNotification=false (default) / Chat??? ???????????? ???????????? ??????
        NotificationBadgeResponse notificationBadgeResponse = new NotificationBadgeResponse();
        List<NotificationRedis> notificationRedisList = notificationRedisTemplate.findByUserId(userId);
        if (!notificationRedisList.isEmpty()) {
            if (!notificationRedisList.get(0).getIsRead()) {
                notificationBadgeResponse.setHasNewNotification(true);
                return notificationBadgeResponse;
            }
        }
        return notificationBadgeResponse;
    }

    public Boolean cleanOfNotificationsOfUser(Long userId) {
        return notificationRedisTemplate.deleteKeyByUserId(userId);
    }

    /** ?????? ????????? FCM ?????? ?????? ?????? */
    public Boolean sendPushNotificationToAll(Long userId, CustomNotificationRequest customNotificationRequest, Long startDeviceId, Long limit) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"User"}, null));
                });

        if (!user.getRole().equals(ADMIN)) throw new UnauthorizedException();

        String messageBody = customNotificationRequest.getMessage();

        try {
            // ???????????? ????????? Device(FCM ??????) ??????
            List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensWithCursor(startDeviceId, limit);

            MulticastMessage message;
            // HINT: ?????? Message ??????
            message = getCustomMulticastMessage(registrationTokens, messageBody);
            // HINT: ????????????????????? Cloud Messaging ??????
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
            return false;
        } catch (Exception e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // ????????? ?????? ?????? ??? ?????? ????????? ????????? ?????? ????????? ??????.
            return false;
        }

        return true;
    }

    public MulticastMessage getCustomMulticastMessage(List<String> registrationTokens, String messageBody) {
        return MulticastMessage.builder()
                .setNotification(builder()
                        .setTitle(NOTIFICATION_TITLE)
                        .setBody(messageBody)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setTitle(NOTIFICATION_TITLE)
                                .setColor("#ffce00")
                                .setBody(messageBody)
                                .setIcon("ic_notification")
                                .setClickAction(ANDROID_CLICK_ACTION)
                                .build())
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setIcon(NOTIFICATION_ICON_URI)
                                .build())
                        .setFcmOptions(WebpushFcmOptions.builder()
                                .setLink("")
                                .build())
                        .build())
                .addAllTokens(registrationTokens)
                .build();
    }
}

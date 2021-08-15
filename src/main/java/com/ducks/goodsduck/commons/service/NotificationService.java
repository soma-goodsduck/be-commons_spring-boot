package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.NotificationRequest;
import com.ducks.goodsduck.commons.model.dto.NotificationResponse;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.entity.UserChat;
import com.ducks.goodsduck.commons.repository.*;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.CHAT;
import static com.google.firebase.messaging.Notification.*;

@Service
@Transactional
@Slf4j
public class NotificationService {

    private final DeviceRepositoryCustom deviceRepositoryCustom;
    private final NotificationRepository notificationRepository;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final UserRepository userRepository;

    public NotificationService(DeviceRepositoryCustomImpl userDeviceRepositoryCustom, NotificationRepository notificationRepository, UserChatRepositoryCustomImpl userChatRepositoryCustom, UserRepository userRepository) {
        this.deviceRepositoryCustom = userDeviceRepositoryCustom;
        this.notificationRepository = notificationRepository;
        this.userChatRepositoryCustom = userChatRepositoryCustom;
        this.userRepository = userRepository;
    }

    public void sendMessage(Notification notification) {

        // 알림 데이터 저장 (DB)
        notificationRepository.save(notification);

        try {
            // 사용자가 등록한 Device(FCM 토큰) 조회
            List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensByUserId(notification.getUser().getId());

            if (registrationTokens.isEmpty()) {
                log.debug("Device for notification not founded.");
                return;
            }

            // HINT: 알림 Message 구성
            MulticastMessage message = getMulticastMessage(notification, registrationTokens)
                    .build();
            log.debug("firebase message is : " + message);

            // HINT: 파이어베이스에 Cloud Messaging 요청
            requestCloudMessagingToFirebase(registrationTokens, message);


        } catch (FirebaseMessagingException e) {
            log.debug(e.getMessage(), e);
//            throw new IOException(e.getMessage()); // 알림은 예외 발생 시 기능 처리에 영향을 주지 않도록 한다.
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
//            throw new IOException(e.getMessage()); // 알림은 예외 발생 시 기능 처리에 영향을 주지 않도록 한다.
        }
    }

    public void sendMessageOfChat(NotificationRequest notificationRequest) throws IOException {

        Notification notification;

        try {
            List<UserChat> userChats = userChatRepositoryCustom.findAllByChatId(notificationRequest.getChatRoomId())
                    .stream()
                    // HINT: SENDER에 해당하는 ROW 제거
                    .filter(userChat -> !userChat.getUser().getId().equals(notificationRequest.getSenderId()))
                    .collect(Collectors.toList());

            if (userChats.isEmpty()) {
                throw new NoResultException("UserChat not founded.");
            } else if (userChats.size() > 1) {
                log.debug("UserChats exist total: " + userChats.size());
            }

            var userChat = userChats.get(0);

            var receiver = userChat.getUser();
            var sender = userRepository.findById(notificationRequest.getSenderId())
                    .orElseThrow(() -> {
                        throw new NoResultException("User who send notification not founded.");
                    });

            notification = new Notification(receiver, sender.getNickName(), sender.getImageUrl(), userChat.getItem().getName(), CHAT);

            notificationRepository.save(notification);

            List<String> registrationTokens = deviceRepositoryCustom.getRegistrationTokensByUserId(receiver.getId());

            // HINT: 알림 Message 구성
            MulticastMessage message = getMulticastMessage(notification, registrationTokens)
                    .putData("charRoomId", notificationRequest.getChatRoomId())
                    .build();

            log.debug("firebase message is : " + message);

            // HINT: 파이어베이스에 Cloud Messaging 요청
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e);
//            throw new IOException(e.getMessage());
        } catch (Exception e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e);
//            throw new IOException(e.getMessage());
        }
    }

    private void requestCloudMessagingToFirebase(List<String> registrationTokens, MulticastMessage message) throws FirebaseMessagingException {
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        log.debug("batch response of firebase is :" + response);
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
                        .setTitle(notificationMessage.getMessageTitle())
                        .setBody(notificationMessage.getMessageBody())
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setTitle(notificationMessage.getMessageTitle()) //
                                .setBody(notificationMessage.getMessageBody())  //
                                .setIcon(notificationMessage.getIconUri())
                                .build())
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.builder()
                                .setLink(notificationMessage.getMessageUri())
                                .build())
                        .build())
                .addAllTokens(registrationTokens);
    }

    public List<NotificationResponse> getNotificationsOfUserId(Long userId) {
        return notificationRepository.findByUserId(userId, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(notification -> new NotificationResponse(notification))
                .collect(Collectors.toList());
    }
}

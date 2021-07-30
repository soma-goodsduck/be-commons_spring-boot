package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.NotificationMessage;
import com.ducks.goodsduck.commons.model.dto.NotificationResponse;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.repository.*;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

import static com.google.firebase.messaging.Notification.*;

@Service
@Transactional
@Slf4j
public class NotificationService {

    private final UserDeviceRepositoryCustom userDeviceRepositoryCustom;
    private final NotificationRepository notificationRepository;

    public NotificationService(UserDeviceRepositoryCustomImpl userDeviceRepositoryCustom, NotificationRepository notificationRepository) {
        this.userDeviceRepositoryCustom = userDeviceRepositoryCustom;
        this.notificationRepository = notificationRepository;
    }

    public void sendMessage(Long receiverUserId, Notification notification) throws IOException {

        try {
            notificationRepository.save(notification);

            List<String> registrationTokens = userDeviceRepositoryCustom.getRegistrationTokensByUserId(receiverUserId);
            NotificationResponse notificationResponse = new NotificationResponse(notification);
            NotificationMessage notificationMessage = notificationResponse.getMessage();

            // HINT: 알림 Message 구성
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(builder()
                            .setTitle(notificationMessage.getMessageTitle())
                            .setBody(notificationMessage.getMessageBody())
                            .setImage(notificationMessage.getImageUri())
                            .build())
                    .addAllTokens(registrationTokens)
                    .build();

            // HINT: 파이어베이스에 Cloud Messaging 요청
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        // The order of responses corresponds to the order of the registration tokens.
                        failedTokens.add(registrationTokens.get(i));
                    }
                }
                log.debug("List of tokens that caused failures: " + failedTokens);
            }
            log.debug(String.format("Completed successful messaging count: %d",  response.getSuccessCount()));

        } catch (FirebaseMessagingException e) {
            log.debug(e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}

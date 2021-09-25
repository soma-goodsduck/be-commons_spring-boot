package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.model.dto.notification.NotificationMessage;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.messaging.Notification.builder;

@Slf4j
public class FcmUtil {
    public static void sendMessage(NotificationMessage notificationMessage, List<String> registrationTokens) {
        final String DOMAIN_ADDRESS = "https://goods-duck.com/";
        final String ANDROID_CLICK_ACTION = "android.intent.action.MAIN";

        try {
            // HINT: 알림 Message 구성
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(builder()
                            .setTitle(notificationMessage.getMessageTitle())
                            .setBody(notificationMessage.getMessageBody())
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(AndroidNotification.builder()
                                    .setTitle(notificationMessage.getMessageTitle())
                                    .setColor("#ffce00")
                                    .setBody(notificationMessage.getMessageBody())
                                    .setIcon("ic_notification")
                                    .setClickAction(ANDROID_CLICK_ACTION)
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
                    .putData("clickAction", notificationMessage.getMessageUri())
                    .build();

            log.debug("FCM message is : " + message.toString());

            // HINT: 파이어베이스에 Cloud Messaging 요청
            requestCloudMessagingToFirebase(registrationTokens, message);

        } catch (FirebaseMessagingException e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // 알림은 예외 발생 시 기능 처리에 영향을 주지 않도록 한다.
        } catch (Exception e) {
            log.debug("exception occured in processing firebase message, \n" + e.getMessage(), e); // 알림은 예외 발생 시 기능 처리에 영향을 주지 않도록 한다.
        }
    }

    private static void requestCloudMessagingToFirebase(List<String> registrationTokens, MulticastMessage message) throws FirebaseMessagingException {
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

}

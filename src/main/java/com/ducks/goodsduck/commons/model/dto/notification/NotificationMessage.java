package com.ducks.goodsduck.commons.model.dto.notification;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class NotificationMessage {

    private String messageTitle;
    private String messageBody;
    private String messageUri;
    private String iconUri;

    public NotificationMessage(String messageTitle, String messageBody, String messageUri, String iconUri) {
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
        this.messageUri = messageUri;
        this.iconUri = iconUri;
    }

    public static NotificationMessage ofLevelUp() {
        return new NotificationMessage(
                "GOODSDUCK",
                "레벨업 하셨습니다!",
                "my-profile",
                "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/image/logo.png"
        );
    }
}

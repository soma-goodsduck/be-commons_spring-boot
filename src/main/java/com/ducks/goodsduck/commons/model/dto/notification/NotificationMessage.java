package com.ducks.goodsduck.commons.model.dto.notification;

import com.ducks.goodsduck.commons.model.entity.Post;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.*;

@Data
@Builder
@NoArgsConstructor
public class NotificationMessage {

    private String messageTitle;
    private String messageBody;
    private String messageUri;
    private String iconUri;
    private NotificationType type;

    public NotificationMessage(String messageTitle, String messageBody, String messageUri, String iconUri, NotificationType notificationType) {
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
        this.messageUri = messageUri;
        this.iconUri = iconUri;
        this.type = notificationType;
    }

    public static NotificationMessage ofLevelUp() {
        return new NotificationMessage(
                "GOODSDUCK",
                "\uD83C\uDF89 LEVEL - UP \uD83C\uDF89",
                "my-profile",
                "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/image/logo.png",
                LEVEL_UP
        );
    }

    public static NotificationMessage ofComment(User sender, Post post, NotificationType commentType) {
        return new NotificationMessage(
                "GOODSDUCK",
                String.format("%s님이 %s를 달았습니다.", sender.getNickName(), commentType.getKorName()),
                String.format("post/%d", post.getId()),
                "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/image/logo.png",
                commentType
        );
    }
}

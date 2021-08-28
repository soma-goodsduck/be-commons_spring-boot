package com.ducks.goodsduck.commons.model.dto.notification;

import com.ducks.goodsduck.commons.model.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationChatRequest {

    private String chatRoomId;
    private Long senderId;
    private NotificationType type;

    public NotificationChatRequest(String chatRoomId, Long senderId, NotificationType type) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.type = type;
    }
}

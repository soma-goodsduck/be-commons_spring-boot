package com.ducks.goodsduck.commons.model.dto.notification;

import com.ducks.goodsduck.commons.model.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationRequest {

    private String chatRoomId;
    private Long senderId;
    private NotificationType type;

    public NotificationRequest(String chatRoomId, Long senderId, NotificationType type) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.type = type;
    }
}

package com.ducks.goodsduck.commons.model.dto.notification;

import lombok.Data;

@Data
public class NotificationBadgeResponse {

    private Boolean hasNewNotification;
    private Boolean hasNewChat;

    public NotificationBadgeResponse() {
        this.hasNewNotification = false;
        this.hasNewChat = false;
    }
}

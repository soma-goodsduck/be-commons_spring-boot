package com.ducks.goodsduck.commons.model.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationBadgeResponse {

    private Boolean hasNewNotification = true;
    private Boolean hasNewChat = true;
}

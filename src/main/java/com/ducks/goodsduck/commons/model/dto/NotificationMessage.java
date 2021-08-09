package com.ducks.goodsduck.commons.model.dto;

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
}

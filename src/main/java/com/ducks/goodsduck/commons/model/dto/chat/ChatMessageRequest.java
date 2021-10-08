package com.ducks.goodsduck.commons.model.dto.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageRequest {

    private String chatMessageId;
    private String chatRoomId;
    private Long senderId;
    private String content;

    public ChatMessageRequest(String chatRoomId, Long senderId, String content) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
    }
}

package com.ducks.goodsduck.commons.model.dto.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRoomResponse {

    private String chatRoomId;
    private Integer size;
    private ChatResponse lastMessage;

    public ChatRoomResponse(String chatRoomId, Integer size, ChatResponse lastMessage) {
        this.chatRoomId = chatRoomId;
        this.size = size;
        this.lastMessage = lastMessage;
    }

    public ChatRoomResponse(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}

package com.ducks.goodsduck.commons.model.dto.chat;

import com.ducks.goodsduck.commons.model.redis.ChatRedis;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatResponse {

    private String id;
    private String chatRoomId;
    private String content;
    private String senderNickName;
    private LocalDateTime createdAt;

    public ChatResponse(ChatRedis chatRedis) {
        this.id = chatRedis.getId();
        this.chatRoomId = chatRedis.getChatRoomId();
        this.content = chatRedis.getContent();
        this.senderNickName = chatRedis.getSenderNickName();
        this.createdAt = chatRedis.getCreatedAt();
    }
}

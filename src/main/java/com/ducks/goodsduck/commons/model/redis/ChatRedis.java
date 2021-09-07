package com.ducks.goodsduck.commons.model.redis;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRedis {
    @Id
    private String id;
    private String chatRoomId;
    private String content;
    private String senderNickName;
    private Boolean isRead;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    public void read() {
        this.isRead = true;
    }

    public ChatRedis(String chatMessageId, String chatRoomId, String content, String senderNickName) {
        this.id = chatMessageId;
        this.chatRoomId = chatRoomId;
        this.content = content;
        this.senderNickName = senderNickName;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}

package com.ducks.goodsduck.commons.model.dto.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRequestDto {

    private String chatId;

    public ChatRequestDto(String chatId) {
        this.chatId = chatId;
    }
}

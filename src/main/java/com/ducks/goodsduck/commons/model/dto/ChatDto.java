package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatDto {

    private String chatId;

    public ChatDto(String chatId) {
        this.chatId = chatId;
    }
}

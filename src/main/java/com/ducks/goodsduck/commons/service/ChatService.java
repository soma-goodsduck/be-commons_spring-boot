package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.chat.ChatResponse;
import com.ducks.goodsduck.commons.model.dto.chat.ChatRoomResponse;
import com.ducks.goodsduck.commons.model.redis.ChatRedis;
import com.ducks.goodsduck.commons.repository.chat.ChatRedisTemplate;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserChatRepository userChatRepository;
    private final ChatRedisTemplate chatRedisTemplate;

    public void findChatRooms(Long userId) {
        userChatRepository.findByUserId(userId);
    }

    public List<ChatRoomResponse> findChatRoomsV2(Long userId) {

        List<String> keys = chatRedisTemplate.scanKeysByUserId(userId);
        List<ChatRoomResponse> chatRoomResponses = new ArrayList<>();

        ChatRoomResponse chatRoomResponse;
        ChatResponse chatResponse;
        ChatRedis chatRedis;
        List<ChatRedis> messages;
        String chatRoomId;
        int size;
        for (String key : keys) {
            messages = chatRedisTemplate.getMessages(key);
            chatRoomId = key.split(":")[3];
            size = messages.size();
            if (size == 0) {
                chatRoomResponse = new ChatRoomResponse(chatRoomId);
            } else {
                chatRedis = messages.get(0);
                chatResponse = new ChatResponse(chatRedis);
                chatRoomResponse = new ChatRoomResponse(chatRoomId, size, chatResponse);
            }

            chatRoomResponses.add(chatRoomResponse);
        }

        return chatRoomResponses;
    }

    public Boolean checkUnreadChat(Long userId) {
        return !chatRedisTemplate.scanKeysByUserId(userId).isEmpty();
    }

    public void readChatMessages(Long userId, String chatRoomId) {
        chatRedisTemplate.removeChatMessages(userId, chatRoomId);
    }
}

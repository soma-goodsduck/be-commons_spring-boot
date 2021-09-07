package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.dto.chat.ChatResponse;
import com.ducks.goodsduck.commons.model.redis.ChatRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ChatRedisTemplate {

    private final RedisTemplate redisTemplate;
    private final ListOperations<String, String> redisDtoListOperations;
    private final ObjectMapper objectMapper;

    private final String PREFIX_OF_USER = "user:";
    private final String PREFIX_OF_CHAT = ":chatRoom:";

    public ChatRedisTemplate(RedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisDtoListOperations = redisTemplate.opsForList();
        this.objectMapper = objectMapper;
    }

    public void saveChatKeyAndValueByUserId(Long userId, ChatRedis chatRedis) throws JsonProcessingException {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_CHAT + chatRedis.getChatRoomId();
        String valueFromChatRedis = objectMapper.writeValueAsString(chatRedis);
        redisTemplate.opsForList().leftPush(key, valueFromChatRedis);
    }

    // Chat
    public List<ChatResponse> findAndPopExceptFirstMessageByUserId(Long userId) {

        String key = PREFIX_OF_USER + userId + PREFIX_OF_CHAT;
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions().match(key).count(5).build();

        Cursor<byte[]> c = redisConnection.scan(options);
        while (c.hasNext()) {
            log.info(new String(c.next()));
        }

        List<String> chatRedisList = redisDtoListOperations.range(key, 0, -1);
        List<ChatResponse> chatResponses = chatRedisList
                .stream()
                .map(stringAsChatRedis -> {
                    ChatRedis chatRedis = null;
                    try {
                        chatRedis = objectMapper.readValue(stringAsChatRedis, ChatRedis.class);
                    } catch (JsonProcessingException e) {
                        log.debug("Failure occurred while processing stringAsJSON value to ChatRedis.class value: ", e);
                        return null;
                    }
                    return new ChatResponse(chatRedis);
                })
                .collect(Collectors.toList());

        int size = chatResponses.size();
        while ( size > 1 ) {
            redisDtoListOperations.rightPop(key);
            size--;
        }

        return chatResponses;
    }

    /**
     * 특정 key 값에 해당하는 리스트 가져오기
     * @param key ("user:{userId}:chatRoom:{chatRoomID}")
     * @return 해당 유저가 받은 ChatMessage가 담긴 리스트
     */
    public List<ChatRedis> getMessages(String key) {

        return redisDtoListOperations.range(key, 0, -1)
                .stream()
                .map(stringAsChatRedis -> {
                    try {
                        return objectMapper.readValue(stringAsChatRedis, ChatRedis.class);
                    } catch (JsonProcessingException e) {
                        log.debug("Failure occurred while processing stringAsJSON value to ChatRedis.class value: ", e);
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * userId에 해당하는 chatRoom과 관련된 key 값들 가져오기
     * @param userId
     * @return key 값이 담긴 리스트 ex. ["user:3:chatRoom:-Mwi3mf2l-wkffm3", "user:3:chatRoom:-M1b3gfrl-vnm4dr", ..]
     */
    public List<String> scanKeysByUserId(Long userId) {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_CHAT;
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions().match(key + "*").count(5).build();

        List<String> keys = new ArrayList<>();
        Cursor<byte[]> c = redisConnection.scan(options);
        while (c.hasNext()) {
            String nextKey = new String(c.next());
            log.info("List of key: " + nextKey);
            keys.add(nextKey);
        }
        return keys;
    }

    /**
     * userId, chatRoomId에 해당하는(key 값 예시: "user:3:chatRoom:-Mwi3mf2l-wkffm3") 전체 삭제(리스트)
     * @param userId
     * @param chatRoomId
     */
    public void removeChatMessages(Long userId, String chatRoomId) {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_CHAT + chatRoomId;
        redisTemplate.delete(key);
    }

}

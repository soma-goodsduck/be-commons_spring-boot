package com.ducks.goodsduck.commons.repository.notification;

import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class NotificationRedisTemplate {

    private final RedisTemplate redisTemplate;
    private final ListOperations<String, String> redisDtoListOperations;
    private final ObjectMapper objectMapper;

    private final String PREFIX_OF_USER = "user:";
    private final String PREFIX_OF_NOTIFICATION = ":notification";

    public NotificationRedisTemplate(RedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisDtoListOperations = redisTemplate.opsForList();
        this.objectMapper = objectMapper;
    }

    public void saveNotificationKeyAndValueByUserId(Long userId, NotificationRedis notificationRedis) throws JsonProcessingException {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        String stringFromNotificationRedis = objectMapper.writeValueAsString(notificationRedis);
        redisTemplate.opsForList().leftPush(key, stringFromNotificationRedis);
    }

    public List<NotificationRedis> findByUserId(Long userId) throws JsonProcessingException {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        return redisDtoListOperations.range(key, 0, -1)
                .stream()
                .map(stringAsNotificationRedis -> {
                    NotificationRedis notificationRedis = null;
                    try {
                        return objectMapper.readValue(stringAsNotificationRedis, NotificationRedis.class);
                    } catch (JsonProcessingException e) {
                        log.debug("Failure occurred while processing stringAsJSON value to NotificationRedis.class value: ", e);
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public void rightPopByUserId(Long userId) {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        redisDtoListOperations.rightPop(key);
    }

    public void setKeyAndValueWithIndexByUserId(Long userId, long index, String stringAsNotificationRedis) {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        redisDtoListOperations.set(key, index, stringAsNotificationRedis);
    }
}

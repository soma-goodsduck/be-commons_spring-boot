package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationRedisResponse;
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

    public List<NotificationRedisResponse> findByUserId(Long userId) throws JsonProcessingException {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        List<NotificationRedis> notificationRedisList = redisDtoListOperations.range(key, 0, -1)
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
                .collect(Collectors.toList());;
        List<NotificationRedisResponse> notificationRedisResponses = notificationRedisList
                .stream()
                .map(notificationRedis -> new NotificationRedisResponse(notificationRedis))
                .collect(Collectors.toList());

        int size = notificationRedisList.size()-1;
        while ( size >= 0 && notificationRedisList.get(size).getExpiredAt().isBefore(LocalDateTime.now())) {
            redisDtoListOperations.rightPop(key);
            size--;
        }

        NotificationRedis notificationRedis;
        String stringAsNotificationRedis;
        for (int i = 0; i <= size; i++) {
            notificationRedis = notificationRedisList.get(i);
            if (notificationRedis.getIsRead()) break;
            notificationRedis.read();
            stringAsNotificationRedis = objectMapper.writeValueAsString(notificationRedis);
            redisDtoListOperations.set(key, i, stringAsNotificationRedis);
        }

        return notificationRedisResponses;
    }
}

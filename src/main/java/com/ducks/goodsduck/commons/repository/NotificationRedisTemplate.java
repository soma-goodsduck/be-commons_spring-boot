package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationRedisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NotificationRedisTemplate {

    private final RedisTemplate redisTemplate;
    private final ListOperations<String, NotificationRedis> redisDtoListOperations;

    private final String PREFIX_OF_USER = "user:";
    private final String PREFIX_OF_NOTIFICATION = ":notification";

    public NotificationRedisTemplate() {
        this.redisTemplate = new RedisTemplate();
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(NotificationRedis.class));
        this.redisDtoListOperations = redisTemplate.opsForList();
    }

    public void saveNotificationKeyAndValueByUserId(Long userId, NotificationRedis notificationRedis) throws JsonProcessingException {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        redisTemplate.opsForList().leftPush(key, notificationRedis);
    }

    public List<NotificationRedisResponse> findByUserId(Long userId) {
        String key = PREFIX_OF_USER + userId + PREFIX_OF_NOTIFICATION;
        List<NotificationRedis> notificationRedisList = redisDtoListOperations.range(key, 0, -1);
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
        for (int i = 0; i <= size; i++) {
            notificationRedis = notificationRedisList.get(i);
            if (notificationRedis.getIsRead()) break;
            notificationRedis.read();
            redisDtoListOperations.set(key, i, notificationRedis);
        }

        return notificationRedisResponses;
    }
}

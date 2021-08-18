package com.ducks.goodsduck.commons.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class SmsAuthenticationRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final String PREFIX = "sms:";
    private final Long TIME_TO_LIMIT = 3 * 60L;

    public void saveKeyAndValue(String phoneNumber, String authenticationNumber) {
        stringRedisTemplate.opsForValue().set(PREFIX + phoneNumber, authenticationNumber, Duration.ofSeconds(TIME_TO_LIMIT));
    }

    public String getValueByPhoneNumber(String phoneNumber) {
        return stringRedisTemplate.opsForValue().get(PREFIX + phoneNumber);
    }

    public void removeKeyAndValue(String phoneNumber) {
        stringRedisTemplate.delete(PREFIX + phoneNumber);
    }

    public boolean hasKey(String phoneNumber) {
        return stringRedisTemplate.hasKey(PREFIX + phoneNumber);
    }
}

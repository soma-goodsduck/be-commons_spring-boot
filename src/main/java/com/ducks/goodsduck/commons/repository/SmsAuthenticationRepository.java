package com.ducks.goodsduck.commons.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class SmsAuthenticationRepository {

    private final StringRedisTemplate stringRedisTemplate;

    private final String PREFIX_OF_SMS = "sms:";
    private final Long TTL_OF_SMS = 3 * 60L;

    public SmsAuthenticationRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveKeyAndValueOfSMS(String phoneNumber, String authenticationNumber) {
        stringRedisTemplate.opsForValue().set(PREFIX_OF_SMS + phoneNumber, authenticationNumber, Duration.ofSeconds(TTL_OF_SMS));
    }

    public String getValueByPhoneNumber(String phoneNumber) {
        return stringRedisTemplate.opsForValue().get(PREFIX_OF_SMS + phoneNumber);
    }

    public void removeKeyAndValueOfSMS(String phoneNumber) {
        stringRedisTemplate.delete(PREFIX_OF_SMS + phoneNumber);
    }

    public boolean hasKey(String phoneNumber) {
        return stringRedisTemplate.hasKey(PREFIX_OF_SMS + phoneNumber);
    }
}

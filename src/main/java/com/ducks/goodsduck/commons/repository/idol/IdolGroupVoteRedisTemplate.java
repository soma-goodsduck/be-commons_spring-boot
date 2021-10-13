package com.ducks.goodsduck.commons.repository.idol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;

@Repository
@Slf4j
public class IdolGroupVoteRedisTemplate {

    private final RedisTemplate<String, String> redisTemplate;
    private final HashOperations<String, Long, Long> redisHashOperations;

    private final String PREFIX_OF_VOTE = "vote:";
    private final String PREFIX_OF_IDOLGROUP = "idolgroup";
    private final String KEY_VOTE_IDOLGROUP = PREFIX_OF_VOTE + PREFIX_OF_IDOLGROUP;

    public IdolGroupVoteRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisTemplate.setKeySerializer(new StringRedisSerializer(UTF_8));
        redisTemplate.setHashKeySerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        this.redisHashOperations = redisTemplate.opsForHash();
    }

    /**
     * 특정 아이돌 그룹 투표 카운트 증가
     * 카운트 없는 경우 key, value 생성 (Hash 구조)
     * @param idolGroupId
     */
    public void addCountByIdolGroupId(Long idolGroupId, Long voteCount) {
        if (!redisHashOperations.hasKey(KEY_VOTE_IDOLGROUP, idolGroupId)) {
            redisHashOperations.put(KEY_VOTE_IDOLGROUP, idolGroupId, voteCount);
            return;
        }
        redisHashOperations.increment(KEY_VOTE_IDOLGROUP, idolGroupId, voteCount);
    }

    public Long findByIdolGroupId(Long idolGroupId) {
        return redisHashOperations.get(KEY_VOTE_IDOLGROUP, idolGroupId);
    }

    /**
     * 아이돌 그룹에 해당하는 전체 투표 기록 삭제
     */
    public Boolean clean() {
        return redisTemplate.delete(KEY_VOTE_IDOLGROUP);
    }

    public Map<Long, Long> findAll() {
        if (!redisTemplate.hasKey(KEY_VOTE_IDOLGROUP)) return new HashMap<>();
        return redisHashOperations.entries(KEY_VOTE_IDOLGROUP);
    }
}

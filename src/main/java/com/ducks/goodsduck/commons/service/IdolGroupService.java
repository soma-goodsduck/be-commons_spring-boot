package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.user.UnauthorizedException;
import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.UserRole;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupRepository;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupVoteRedisTemplate;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IdolGroupService {

    private final IdolGroupRepository idolGroupRepository;
    private final UserRepository userRepository;
    private final IdolGroupVoteRedisTemplate idolGroupVoteRedisTemplate;

    private final MessageSource messageSource;

    public List<IdolGroup> getIdolGroups() {
        Map<Long, Long> idolGroupVoteMap = idolGroupVoteRedisTemplate.findAll();
        return idolGroupRepository.findAll()
                .stream()
                .map(idolGroup -> {
                    Long idolGroupId = idolGroup.getId();
                    if (idolGroupVoteMap.containsKey(idolGroupId)) idolGroup.setVotedCount(idolGroupVoteMap.get(idolGroupId));
                    else idolGroup.setVotedCount(0L);
                    return idolGroup;
                })
                .collect(Collectors.toList());
    }

    public Optional<IdolGroup> getIdolGroup(Long idolGroupId) {
        return idolGroupRepository.findById(idolGroupId);
    }

    public Boolean voteIdolGroup(Long userId, Long idolGroupId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        idolGroupVoteRedisTemplate.addCountByIdolGroupId(idolGroupId);
        user.vote(idolGroupId);
        return true;
    }

    public Boolean cleanVote(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        if (!user.getRole().equals(UserRole.ADMIN)) throw new UnauthorizedException();
        return idolGroupVoteRedisTemplate.clean();
    }
}

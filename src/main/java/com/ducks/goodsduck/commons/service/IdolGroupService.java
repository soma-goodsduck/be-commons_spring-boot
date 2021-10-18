package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.InvalidRequestDataException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.user.UnauthorizedException;
import com.ducks.goodsduck.commons.model.dto.VoteResponse;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupWithVote;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupWithVotes;
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

import javax.persistence.EntityManager;
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
    private final EntityManager em;
    private final IdolGroupVoteRedisTemplate idolGroupVoteRedisTemplate;

    private final MessageSource messageSource;

    public List<IdolGroup> getIdolGroups() {
        return idolGroupRepository.findAll();
    }

    public IdolGroupWithVotes getIdolGroupsWithVote(Long userId) {
        Map<Long, Long> idolGroupVoteMap = idolGroupVoteRedisTemplate.findAllVoteOfIdolGroup();
        List<IdolGroupWithVote> idolGroupsWithVotes = idolGroupRepository.findAll()
                .stream()
                .map(idolGroup -> {
                    Long idolGroupId = idolGroup.getId();
                    IdolGroupWithVote idolGroupWithVote = new IdolGroupWithVote(idolGroup);
                    if (idolGroupVoteMap.containsKey(idolGroupId))
                        idolGroupWithVote.setVotedCount(idolGroupVoteMap.get(idolGroupId));
                    else idolGroupWithVote.setVotedCount(0L);
                    return idolGroupWithVote;
                })
                .collect(Collectors.toList());

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                                new Object[]{"User"}, null)));


        return new IdolGroupWithVotes(idolGroupsWithVotes, user.getNumberOfVotes());
    }

    public Optional<IdolGroup> getIdolGroup(Long idolGroupId) {
        return idolGroupRepository.findById(idolGroupId);
    }

    public VoteResponse voteIdolGroup(Long userId, Long idolGroupId, Long voteCount) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        if (user.getNumberOfVotes() >= voteCount) {
            idolGroupVoteRedisTemplate.addCountByIdolGroupId(idolGroupId, voteCount);
            user.vote(idolGroupId, voteCount);
        } else throw new InvalidRequestDataException("Votes user have is must be more than voteCount.");

        // Batch 설정으로 인해 flush 처리
        em.flush();
        em.clear();

        return new VoteResponse(idolGroupId, voteCount);
    }

    public Boolean cleanVote(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        if (!user.getRole().equals(UserRole.ADMIN)) throw new UnauthorizedException();
        return idolGroupVoteRedisTemplate.cleanVotesOfIdolGroup();
    }
}

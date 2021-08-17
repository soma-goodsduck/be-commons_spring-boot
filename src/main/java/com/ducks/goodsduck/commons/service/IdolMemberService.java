package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.model.entity.IdolMember;
import com.ducks.goodsduck.commons.repository.IdolMemberRepository;
import com.ducks.goodsduck.commons.repository.IdolMemberRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IdolMemberService {

    private final IdolMemberRepository idolMemberRepository;
    private final IdolMemberRepositoryCustom idolMemberRepositoryCustom;

    public List<IdolMember> findIdolMembersOfGroup(Long idolGroupId) {
        return idolMemberRepositoryCustom.findAllByIdolGroupId(idolGroupId)
                .stream()
                .map(tuple -> {
                    IdolMember idolMember = tuple.get(0, IdolMember.class);
                    IdolGroup idolGroup = tuple.get(1, IdolGroup.class);
                    return idolMember;
                })
                .collect(Collectors.toList());
    }

    public List<IdolMember> findAllIdolMembers() {
        return idolMemberRepositoryCustom.findAll()
                .stream()
                .map(tuple -> {
                    IdolMember idolMember = tuple.get(0, IdolMember.class);
                    IdolGroup idolGroup = tuple.get(1, IdolGroup.class);
                    return idolMember;
                })
                .collect(Collectors.toList());
    }

    public Optional<IdolMember> findIdolMemberById(Long idolMemberId) {
        return idolMemberRepository.findById(idolMemberId);
    }
}

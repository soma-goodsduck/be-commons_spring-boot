package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.repository.IdolGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IdolGroupService {

    private final IdolGroupRepository idolGroupRepository;

    public List<IdolGroup> getIdolGroups() { return idolGroupRepository.findAll(); }

    public Optional<IdolGroup> getIdolGroup(Long idolGroupId) {
        return idolGroupRepository.findById(idolGroupId);
    }
}

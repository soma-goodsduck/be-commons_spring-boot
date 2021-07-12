package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.IdolGroup;
import com.ducks.goodsduck.commons.repository.IdolGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IdolGroupService {

    private final IdolGroupRepository idolGroupRepository;

    public List<IdolGroup> getIdolGroups() {
        return idolGroupRepository.findAll();
    }
}

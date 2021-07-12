package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.IdolGroupDto;
import com.ducks.goodsduck.commons.service.IdolGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IdolGroupController {

    private final IdolGroupService idolGroupService;

    /** 아이돌 그룹 리스트 가져오기 API */
    @GetMapping("/idolgroup")
    public List<IdolGroupDto> getIdolGroups() {
        return idolGroupService.getIdolGroups()
                .stream()
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .collect(Collectors.toList());
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupDto;
import com.ducks.goodsduck.commons.model.dto.idol.IdolMemberDto;
import com.ducks.goodsduck.commons.service.IdolGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IdolGroupController {

    private final IdolGroupService idolGroupService;

    /** 아이돌 그룹 리스트 가져오기 API */
    @NoCheckJwt
    @GetMapping("/idol")
    public List<IdolGroupDto> getIdolGroups() {
        return idolGroupService.getIdolGroups()
                .stream()
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .sorted(Comparator.comparing(IdolGroupDto::getName))
                .collect(Collectors.toList());
    }

    /** 아이돌 그룹 가져오기 API */
    @NoCheckJwt
    @GetMapping("/idol/{idol_group_id}")
    public IdolGroupDto getIdolGroup(@PathVariable("idol_group_id") Long idolGroupId) {
        return idolGroupService.getIdolGroup(idolGroupId)
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .orElseGet(() -> new IdolGroupDto());
    }
}

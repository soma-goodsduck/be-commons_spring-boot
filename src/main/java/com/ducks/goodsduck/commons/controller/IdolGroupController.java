package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupDto;
import com.ducks.goodsduck.commons.service.IdolGroupService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IdolGroupController {

    private final IdolGroupService idolGroupService;

    @GetMapping("/idol")
    @ApiOperation("아이돌 그룹 리스트 가져오기 API")
    public List<IdolGroupDto> getIdolGroups() {
        return idolGroupService.getIdolGroups()
                .stream()
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .collect(Collectors.toList());
    }

    @GetMapping("/idol/{idol_group_id}")
    @ApiOperation("아이돌 그룹 가져오기 API")
    public IdolGroupDto getIdolGroup(@PathVariable("idol_group_id") Long idolGroupId) {
        return idolGroupService.getIdolGroup(idolGroupId)
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .orElseGet(() -> new IdolGroupDto());
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupDto;
import com.ducks.goodsduck.commons.model.dto.idol.IdolMemberDto;
import com.ducks.goodsduck.commons.service.IdolGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Api(tags = "아이돌 그룹 APIs")
public class IdolGroupController {

    private final IdolGroupService idolGroupService;

    @NoCheckJwt
    @GetMapping("/v1/idol-groups")
    @ApiOperation("아이돌 그룹 리스트 가져오기 API")
    public ApiResult<List<IdolGroupDto>> getIdolGroups() {
        return OK(idolGroupService.getIdolGroups()
                .stream()
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .sorted(Comparator.comparing(IdolGroupDto::getName))
                .collect(Collectors.toList()));
    }

    @NoCheckJwt
    @GetMapping("/v1/idol-groups/{idolGroupId}")
    @ApiOperation("아이돌 그룹 가져오기 API")
    public ApiResult<IdolGroupDto> getIdolGroup(@PathVariable("idolGroupId") Long idolGroupId) {
        return OK(idolGroupService.getIdolGroup(idolGroupId)
                .map(idolGroup -> new IdolGroupDto(idolGroup))
                .orElseGet(() -> new IdolGroupDto()));
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupDto;
import com.ducks.goodsduck.commons.service.IdolGroupService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/v1/idol-groups/{idolGroupId}/vote")
    @ApiOperation("특정 아이돌 그룹에 투표하기 API")
    public ApiResult<Boolean> voteIdolGroup(HttpServletRequest request, @PathVariable("idolGroupId") Long idolGroupId) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(idolGroupService.voteIdolGroup(userId, idolGroupId));
    }

    @DeleteMapping("/v1/idol-groups/vote")
    @ApiOperation("(관리자용) 아이돌 그룹 투표 정보 비우기 API")
    public ApiResult<Boolean> cleanVote(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(idolGroupService.cleanVote(userId));
    }
}

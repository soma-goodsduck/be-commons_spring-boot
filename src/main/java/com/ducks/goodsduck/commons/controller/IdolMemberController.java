package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.idol.IdolMemberDto;
import com.ducks.goodsduck.commons.service.IdolMemberService;
import io.swagger.annotations.Api;
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
@Api(tags = "아이돌 멤버 APIs")
public class IdolMemberController {

    private final IdolMemberService idolMemberService;

    @NoCheckJwt
    @GetMapping("/idol/{idol_group_id}/member")
    @ApiOperation("특정 아이돌 그룹에 해당하는 아이돌 멤버 리스트 가져오기 API")
    public List<IdolMemberDto> getIdolMembersOfGroup(@PathVariable("idol_group_id") Long idolGroupId) {
        return idolMemberService.findIdolMembersOfGroup(idolGroupId)
                .stream()
                .map(idolMember -> new IdolMemberDto(idolMember))
                .collect(Collectors.toList());
    }

    @NoCheckJwt
    @GetMapping("/idol/member")
    @ApiOperation("아이돌 멤버 리스트 가져오기 API")
    public List<IdolMemberDto> getIdolMemberList() {
        return idolMemberService.findAllIdolMembers()
                .stream()
                .map(idolMember -> new IdolMemberDto(idolMember))
                .collect(Collectors.toList());
    }

    @NoCheckJwt
    @GetMapping("/idol/member/{idol_member_id}")
    @ApiOperation("특정 아이돌 멤버 가져오기 API")
    public IdolMemberDto getIdolMember(@PathVariable("idol_member_id") Long idolMemberId) {
        return idolMemberService.findIdolMemberById(idolMemberId)
                .map(idolMember -> new IdolMemberDto(idolMember))
                .orElseGet(() -> new IdolMemberDto());
    }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.IdolMemberDto;
import com.ducks.goodsduck.commons.service.IdolMemberService;
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
public class IdolMemberController {

    private final IdolMemberService idolMemberService;

    /** 특정 아이돌 그룹에 해당하는 아이돌 멤버 리스트 가져오기 API */
    @GetMapping("/idol/{idol_group_id}/member")
    public List<IdolMemberDto> getIdolMembersOfGroup(@PathVariable("idol_group_id") Long idolGroupId) {
        return idolMemberService.findIdolMembersOfGroup(idolGroupId)
                .stream()
                .map(idolMember -> new IdolMemberDto(idolMember))
                .collect(Collectors.toList());
    }

    /** 아이돌 멤버 리스트 가져오기 API */
    @GetMapping("/idol/member")
    public List<IdolMemberDto> getIdolMemberList() {
        return idolMemberService.findAllIdolMembers()
                .stream()
                .map(idolMember -> new IdolMemberDto(idolMember))
                .collect(Collectors.toList());
    }

    /** 특정 아이돌 멤버 가져오기 API */
    @GetMapping("/idol/member/{idol_member_id}")
    public IdolMemberDto getIdolMember(@PathVariable("idol_member_id") Long idolMemberId) {
        return idolMemberService.findIdolMemberById(idolMemberId)
                .map(idolMember -> new IdolMemberDto(idolMember))
                .orElseGet(() -> new IdolMemberDto());
    }


}

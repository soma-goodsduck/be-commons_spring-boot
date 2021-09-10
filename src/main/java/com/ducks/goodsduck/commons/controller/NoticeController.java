package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.NoticeDto;
import com.ducks.goodsduck.commons.model.dto.idol.IdolGroupDto;
import com.ducks.goodsduck.commons.repository.NoticeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "공지사항 APIs")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @NoCheckJwt
    @GetMapping("/v1/notices")
    @ApiOperation(value = "공지사항 조회 (비회원)")
    public List<NoticeDto> getNotices() {
        return noticeRepository.findAll()
                .stream()
                .map(notice -> new NoticeDto(notice))
                .sorted(Comparator.comparing(NoticeDto::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }
}

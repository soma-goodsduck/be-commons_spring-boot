package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.NoticeDto;
import com.ducks.goodsduck.commons.model.dto.notification.CustomNotificationRequest;
import com.ducks.goodsduck.commons.repository.NoticeRepository;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "공지사항 APIs")
public class NoticeController {

    private final NoticeRepository noticeRepository;
    private final NotificationService notificationService;

    @NoCheckJwt
    @GetMapping("/v1/notices")
    @ApiOperation(value = "공지사항 조회 (비회원)")
    public ApiResult<List<NoticeDto>> getNotices() {
        return OK(noticeRepository.findAll()
                .stream()
                .map(notice -> new NoticeDto(notice))
                .sorted(Comparator.comparing(NoticeDto::getUpdatedAt).reversed())
                .collect(Collectors.toList()));
    }


    @GetMapping("/v1/notices/push")
    @ApiOperation(value = "사용자 전체에 푸시 알림 전송 API (관리자용)")
    public ApiResult<Boolean> sendPushNotificationsToAll(HttpServletRequest request, @RequestBody CustomNotificationRequest customNotificationRequest) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(notificationService.sendPushNotificationToAll(userId, customNotificationRequest));
    }
}

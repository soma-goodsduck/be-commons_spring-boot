package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.NotificationRequest;
import com.ducks.goodsduck.commons.model.dto.chat.ChatAndItemDto;
import com.ducks.goodsduck.commons.model.dto.chat.ChatRequestDto;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatDto;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.service.UserChatService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Api(tags = "채팅 APIs")
public class ChatController {

    private final UserChatService userChatService;
    private final UserService userService;
    private final NotificationService notificationService;

    @ApiOperation("채팅방 생성 API by 즉시 판매/구매")
    @PostMapping("/v1/chat/items/{itemId}")
    public ApiResult<Boolean> createChatWithImmediateTrade(@PathVariable("itemId") Long itemId,
                                                           @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithImmediateTrade(chatDto.getChatId(), userId, itemId));
    }

    @ApiOperation("채팅방 생성 API by 가격 제안 수락")
    @PostMapping("/v1/chat/price-propose/{priceProposeId}")
    public ApiResult<Boolean> createChatWithPricePropose(@PathVariable("priceProposeId") Long priceProposeId,
                                                         @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        Long itemOwnerId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithPricePropose(chatDto.getChatId(), itemOwnerId, priceProposeId));
    }

    @ApiOperation("채팅방 나갔을 때, 채팅방 삭제 API")
    @DeleteMapping("/v1/chat")
    public ApiResult<Boolean> deleteChat(@RequestBody ChatRequestDto chatDto) throws Exception {
        return OK(userChatService.deleteChat(chatDto.getChatId()));
    }

    @ApiOperation("채팅방 이미지 업로드 API")
    @PostMapping("/v1/users/chat-image")
    public ApiResult<String> uploadChatImage(@RequestParam MultipartFile multipartFile) throws IOException {
        return OK(userService.uploadChatImage(multipartFile, ImageType.CHAT));
    }


    @ApiOperation("채팅 전송 시 알림 요청 API")
    @PostMapping("/v1/chat/notification")
    public ApiResult<Boolean> sendNotification(@RequestBody NotificationRequest notificationRequest) throws IOException {
        notificationService.sendMessageOfChat(notificationRequest);
        return OK(true);
    }

    @ApiOperation("(삭제예정) 채팅방 ID를 통한 User 정보 획득 API")
    @GetMapping("/v1/chat/{chatId}")
    public ApiResult<UserChatDto> getChatInfo(@PathVariable("chatId") String chatId, HttpServletRequest request) throws IllegalAccessException {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatInfo(chatId, userId));
    }

    @ApiOperation("(삭제예정) 유저가 속해있는 채팅방 정보 획득 API")
    @GetMapping("/v1/chat")
    public ApiResult<List<ChatAndItemDto>> getChatInfoOfUser(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatInfoOfUser(userId));
    }
}

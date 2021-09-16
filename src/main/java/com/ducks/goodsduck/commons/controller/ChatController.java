package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.chat.*;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationRequest;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.service.ChatService;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.service.UserChatService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
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
    private final ChatService chatService;

    @ApiOperation("채팅방 생성 API by 즉시 판매/구매")
    @PostMapping("/v1/chat/items/{itemId}")
    public ApiResult<Boolean> createChatWithImmediateTrade(@PathVariable("itemId") Long itemId,
                                                           @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithImmediateTrade(chatDto.getChatId(), userId, itemId));
    }

    @ApiOperation("채팅방 생성 API by 가격 제안 수락")
    @PostMapping("/v1/chat/price-propose/{priceProposeId}")
    public ApiResult<Boolean> createChatWithPricePropose(@PathVariable("priceProposeId") Long priceProposeId,
                                                         @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        var itemOwnerId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithPricePropose(chatDto.getChatId(), itemOwnerId, priceProposeId));
    }

    @ApiOperation("채팅방 나갔을 때, 채팅방 삭제 API")
    @PostMapping("/v1/chat")
    public ApiResult<Boolean> deleteChat(@RequestBody ChatRequestDto chatDto) throws Exception {
        return OK(userChatService.deleteChat(chatDto.getChatId()));
    }

    @ApiOperation("채팅방 이미지 업로드 API")
    @PostMapping("/v1/users/chat-image")
    public ApiResult<String> uploadChatImage(@RequestParam MultipartFile multipartFile, HttpServletRequest request) throws IOException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userService.uploadChatImage(multipartFile, ImageType.CHAT, userId));
    }

    @ApiOperation("채팅 전송 시 알림 요청 API")
    @PostMapping("/v1/chat/notification")
    public ApiResult<Boolean> sendNotification(HttpServletRequest request, @RequestBody NotificationRequest notificationRequest) throws IOException, IllegalAccessException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        notificationService.sendMessageOfChat(userId, notificationRequest);
        return OK(true);
    }

    @ApiOperation("채팅 전송 시 알림 요청 API")
    @PostMapping("/v2/chat/notification")
    public ApiResult sendNotificationV2(HttpServletRequest request, @RequestBody ChatMessageRequest chatMessageRequest) throws IOException, IllegalAccessException, FirebaseMessagingException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        notificationService.sendMessageOfChatV2(userId, chatMessageRequest);
        return OK(true);
    }

    @ApiOperation("유저가 알림 받은 채팅방 목록 조회 API")
    @GetMapping("/v1/users/chats")
    public ApiResult getChatRooms(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        chatService.findChatRooms(userId);
        return OK(true);
    }

    @ApiOperation("유저가 알림 받은 채팅방 목록 조회 API")
    @GetMapping("/v2/users/chats")
    public ApiResult<List<ChatRoomResponse>> getChatRoomsV2(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(chatService.findChatRoomsV2(userId));
    }

    @ApiOperation("채팅방 접속 시 채팅 읽음 처리 API")
    @DeleteMapping("/v1/users/chats/{chatRoomId}")
    public ApiResult readChatMessages(HttpServletRequest request, @PathVariable("chatRoomId") String chatRoomId) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        chatService.readChatMessages(userId, chatRoomId);
        return OK(true);
    }

    @ApiOperation("안 읽은 채팅 메시지 여부 조회 API")
    @GetMapping("/v1/users/chats/noty")
    public ApiResult checkUnreadChat(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(chatService.checkUnreadChat(userId));
    }

    @ApiOperation("거래 요청한 입장인 채팅방 목록 조회 API (아이템 주인이 아닌 경우에 한함)")
    @GetMapping("/v2/users/chat-rooms")
    public ApiResult<List<ChatRoomDto>> getChatRoomsWithNowOwner(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatRoomsWithNotOwner(userId));
    }

    @ApiOperation("(삭제예정) 채팅방 ID를 통한 User 정보 획득 API")
    @GetMapping("/v1/chat/{chatId}")
    public ApiResult<UserChatDto> getChatInfo(@PathVariable("chatId") String chatId, HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatInfo(chatId, userId));
    }

    @ApiOperation("(삭제예정) 유저가 속해있는 채팅방 정보 획득 API")
    @GetMapping("/v1/chat")
    public ApiResult<List<ChatRoomDto>> getChatInfoOfUser(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatRooms(userId));
    }

    @ApiOperation("유저가 속해있는 채팅방 정보 획득 API")
    @GetMapping("/v2/chat")
    public ApiResult<List<ChatRoomDto>> getChatInfoOfUserV2(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatRoomsV2(userId));
    }
}

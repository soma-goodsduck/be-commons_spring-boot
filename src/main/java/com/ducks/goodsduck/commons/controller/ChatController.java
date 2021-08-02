package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.chat.ChatAndItemDto;
import com.ducks.goodsduck.commons.model.dto.chat.ChatRequestDto;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatDto;
import com.ducks.goodsduck.commons.service.UserChatService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Api(tags = "채팅 APIs")
public class ChatController {

    private final UserChatService userChatService;
    
    // 유저가 참여하고 있는 채팅방 아이디

    @ApiOperation("채팅방 생성 API by 즉시 판매/구매 API")
    @PostMapping("/v1/chat/items/{itemId}")
    public ApiResult<Boolean> createChatWithImmediateTrade(@PathVariable("itemId") Long itemId,
                                                           @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithImmediateTrade(chatDto.getChatId(), userId, itemId));
    }

    @ApiOperation("채팅방 생성 API by 가격 제안 수락 API")
    @PostMapping("/v1/chat/price-propose/{priceProposeId}")
    public ApiResult<Boolean> createChatWithPricePropose(@PathVariable("priceProposeId") Long priceProposeId,
                                                         @RequestBody ChatRequestDto chatDto, HttpServletRequest request) {

        Long itemOwnerId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.createWithPricePropose(chatDto.getChatId(), itemOwnerId, priceProposeId));
    }
    
    @ApiOperation("채팅방 ID를 통한 User 정보 획득 API")
    @GetMapping("/v1/chat/{chatId}")
    public ApiResult<UserChatDto> getChatInfo(@PathVariable("chatId") String chatId, HttpServletRequest request) throws IllegalAccessException {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatInfo(chatId, userId));
    }

    @ApiOperation("유저가 속해있는 채팅방 정보 획득 API")
    @GetMapping("/v1/chat")
    public ApiResult<List<ChatAndItemDto>> getChatInfoOfUser(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userChatService.getChatInfoOfUser(userId));
    }
}

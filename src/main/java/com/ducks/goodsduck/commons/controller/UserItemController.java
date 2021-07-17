package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.LikeItemResponse;
import com.ducks.goodsduck.commons.service.JwtService;
import com.ducks.goodsduck.commons.service.UserItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserItemController {

    private static final String KEY_OF_USERID_IN_JWT_PAYLOADS = "userId";
    private final UserItemService userItemService;
    private final JwtService jwtService;

    /** 특정 아이템 좋아요 요청 API */
    @GetMapping("/like/item/{item_id}")
    public LikeItemResponse doLikeItem(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId) {
        //TODO 여기서 JWT를 받아와서 로그인한 유저 ID를 파악하는 로직이 필요한지 체크
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.doLike(userId, itemId);
    }

    /** 좋아요 취소 요청 API */
    @DeleteMapping("/like/item/{item_id}")
    public boolean cancleLikeItem(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId) {
        //TODO 여기서 JWT를 받아와서 로그인한 유저 ID를 파악하는 로직이 필요한지 체크
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.cancelLikeItem(userId, itemId);
    }

    /** 좋아요한 아이템 목록 보기 API */
    @GetMapping("/like/item")
    public List<ItemDto> getLikeItems(@RequestHeader("jwt") String jwt) {
        //TODO 여기서 JWT를 받아와서 로그인한 유저 ID를 파악하는 로직이 필요한지 체크
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.getLikeItemsOfUser(userId);
    }
}

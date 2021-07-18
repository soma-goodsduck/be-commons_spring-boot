package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.LikeItemResponse;
import com.ducks.goodsduck.commons.service.UserItemService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserItemController {

    private final UserItemService userItemService;

    @GetMapping("/like/item/{item_id}")
    @ApiOperation("특정 아이템 좋아요 요청 API")
    public LikeItemResponse doLikeItem(@PathVariable("item_id") Long itemId,
                                       HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.doLike(userId, itemId);
    }

    @DeleteMapping("/like/item/{item_id}")
    @ApiOperation("좋아요 취소 요청 API")
    public boolean cancleLikeItem(@PathVariable("item_id") Long itemId,
                                  HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.cancelLikeItem(userId, itemId);
    }

    @GetMapping("/like/item")
    @ApiOperation("좋아요한 아이템 목록 보기 API")
    public List<ItemDto> getLikeItems(HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return userItemService.getLikeItemsOfUser(userId);
    }
}

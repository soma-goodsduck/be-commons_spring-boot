package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.service.UserItemService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Api(tags = "아이템 게시글 좋아요 APIs")
public class UserItemController {

    private final UserItemService userItemService;

    @GetMapping("/like/item/{item_id}")
    @ApiOperation("특정 아이템 좋아요 요청 API")
    public ApiResult doLikeItem(@PathVariable("item_id") Long itemId,
                                                 HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.doLike(userId, itemId));
    }

    @DeleteMapping("/like/item/{item_id}")
    @ApiOperation("좋아요 취소 요청 API")
    public ApiResult cancleLikeItem(@PathVariable("item_id") Long itemId,
                                  HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.cancelLikeItem(userId, itemId));
    }

    @GetMapping("/like/item")
    @ApiOperation("좋아요한 아이템 목록 보기 API")
    public ApiResult<List<ItemDto>> getLikeItems(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.getLikeItemsOfUser(userId));
    }
}

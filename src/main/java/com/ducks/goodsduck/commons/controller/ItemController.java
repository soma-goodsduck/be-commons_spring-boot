package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.*;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationBadgeResponse;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserItem;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.repository.category.ItemCategoryRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.image.ItemImageRepository;
import com.ducks.goodsduck.commons.service.*;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;
import static com.ducks.goodsduck.commons.model.enums.TradeStatus.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "아이템 CRUD APIs")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final UserItemService userItemService;
    private final NotificationService notificationService;

    // TODO : 워터마크 테스트용 추후 삭제
    private final ImageUploadService imageUploadService;
//    @NoCheckJwt
//    @ApiOperation(value = "(테스트 중) 워터마크 테스트 API")
//    @PostMapping("/v1/check/watermark")
//    public Long checkWatermark(@RequestParam MultipartFile multipartFile) throws IOException {
//
//        imageUploadService.uploadImageWithWatermark(multipartFile);
//        return 1L;
//    }

    @NoCheckJwt
    @PostMapping("/v1/gif")
    public ApiResult resizeGIF(@RequestParam MultipartFile multipartFile) throws IOException {
        imageUploadService.resizeGIF(multipartFile);
        return OK(true);
    }

    @ApiOperation(value = "아이템 등록하기")
    @PostMapping("/v1/items")
    public ApiResult<Long> uploadItem(@RequestParam String stringItemDto,
                                      @RequestParam List<MultipartFile> multipartFiles,
                                      HttpServletRequest request) throws IOException {

        ItemUploadRequest itemUploadRequest = new ObjectMapper().readValue(stringItemDto, ItemUploadRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(itemService.upload(itemUploadRequest, multipartFiles, userId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 상세보기")
    @GetMapping("/v1/items/{itemId}")
    public ApiResult<ItemDetailResponse> showItemDetail(@PathVariable("itemId") Long itemId, @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 상세보기
        if(userId.equals(-1L)) {
            return OK(itemService.showDetail(itemId));
        }
        // HINT : 회원에게 보여줄 상세보기
        else {
            return OK(itemService.showDetailWithLike(userId, itemId));
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 상세보기 (요약)")
    @GetMapping("/v1/items/{itemId}/summary")
    public ApiResult<ItemSummaryDto> showItemDetailSummary(@PathVariable("itemId") Long itemId) {
        return OK(itemService.showDetailSummary(itemId));
    }

    @ApiOperation(value = "아이템 수정")
    @PutMapping("/v1/items/{itemId}")
    public ApiResult<Long> editItem(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto) throws JsonProcessingException {
        ItemUpdateRequest itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequest.class);
        return OK(itemService.edit(itemId, itemUpdateRequest));
    }

    @ApiOperation(value = "아이템 수정 V2")
    @PutMapping("/v2/items/{itemId}")
    public ApiResult<Long> editItemV2(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles,
                                      HttpServletRequest request) throws JsonProcessingException {

        ItemUpdateRequestV2 itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequestV2.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(itemService.editV2(itemId, itemUpdateRequest, multipartFiles, userId));
    }

    @ApiOperation(value = "아이템 삭제")
    @DeleteMapping("/v1/items/{itemId}")
    public ApiResult<Long> deleteItem(@PathVariable("itemId") Long itemId) {
        return OK(itemService.delete(itemId));
    }

    @ApiOperation(value = "아이템 삭제 V2")
    @DeleteMapping("/v2/items/{itemId}")
    public ApiResult<Long> deleteItemV2(@PathVariable("itemId") Long itemId) { return OK(itemService.deleteV2(itemId)); }

    @NoCheckJwt
    @ApiOperation(value = "아이템 검색 (회원/비회원)")
    @GetMapping("/v1/items/search")
    @Transactional
    public ApiResult<HomeResponse<ItemHomeResponse>> getSearchedItems(@RequestParam("keyword") String keyword,
                                                                      @RequestParam("itemId") Long itemId,
                                                                      @RequestHeader("jwt") String jwt) {
        Long userId = userService.checkLoginStatus(jwt);
        return OK(itemService.getSearchedItemList(userId, keyword, itemId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 조회 API in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items")
    @Transactional
    public ApiResult<HomeResponse<ItemHomeResponse>> getItemList(@RequestParam("itemId") Long itemId, @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);
        HomeResponse homeResponse = itemService.getItemListV3(userId, itemId);
        homeResponse.setNoty(notificationService.checkNewNotification(userId));
        return OK(homeResponse);
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 조회 + 아이돌 그룹 필터링 API in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items/filter")
    @Transactional
    public ApiResult<HomeResponse<ItemHomeResponse>> getItemListFilterByIdolGroupV3(@RequestParam("idolGroup") Long idolGroupId,
                                                                                    @RequestParam("itemId") Long itemId,
                                                                                    @RequestHeader("jwt") String jwt) {
        Long userId = userService.checkLoginStatus(jwt);
        HomeResponse homeResponse = itemService.getItemListFilterByIdolGroupV3(userId, idolGroupId, itemId);
        homeResponse.setNoty(notificationService.checkNewNotification(userId));
        return OK(homeResponse);
    }

    // TODO : 이후에 거래완료 필터링에서 제거
    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 조회 + 전체 필터링 API in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items/filters")
    @Transactional
    public ApiResult<HomeResponse<ItemHomeResponse>> getItemListFilterByAllV3(@RequestParam(value = "idolGroup") Long idolGroupId,
                                                                              @RequestParam(value = "idolMember", required = false) List<Long> idolMembersId,
                                                                              @RequestParam(value = "tradeType", required = false) TradeType tradeType,
                                                                              @RequestParam(value = "category", required = false) Long itemCategoryId,
                                                                              @RequestParam(value = "gradeStatus", required = false) GradeStatus gradeStatus,
                                                                              @RequestParam(value = "minPrice", required = false) Long minPrice,
                                                                              @RequestParam(value = "maxPrice", required = false) Long maxPrice,
                                                                              @RequestParam("itemId") Long itemId, @RequestHeader("jwt") String jwt) {
        Long userId = userService.checkLoginStatus(jwt);
        ItemFilterDto itemFilterDto = new ItemFilterDto(idolGroupId, idolMembersId, tradeType, itemCategoryId, gradeStatus, minPrice, maxPrice);
        HomeResponse homeResponse = itemService.getItemListFilterByAllV3(userId, itemFilterDto, itemId);
        homeResponse.setNoty(notificationService.checkNewNotification(userId));
        return OK(homeResponse);
    }

    @NoCheckJwt
    @ApiOperation(value = "제품상태 리스트 불러오기 in 아이템 등록")
    @GetMapping("/v1/items/grade-status")
    public ApiResult<List<GradeStatusDto>> getGradeStatusItem() {
        return OK(Arrays.asList(GradeStatus.values())
                .stream()
                .map(gradeStatus -> new GradeStatusDto(gradeStatus))
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "아이템 거래 상태 변경 API")
    @PatchMapping("/v1/items/{itemId}/trade-status")
    public ApiResult updateMyItemTradeStatus(HttpServletRequest request, @PathVariable("itemId") Long item_id, @RequestBody ItemTradeStatusUpdateRequest tradeStatus) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        TradeStatus status;

        try {
            status = valueOf(tradeStatus.getTradeStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Exception occurred in parsing tradeStatus: {}", e.getMessage(), e);
            throw new IllegalArgumentException("There is no tradeStatus inserted");
        }

        return OK(itemService.updateTradeStatus(userId, item_id, status));
    }

    @PostMapping("/v1/items/{itemId}/like")
    @ApiOperation("특정 아이템 좋아요 요청 API")
    public ApiResult<UserItemResponse> doLikeItem(@PathVariable("itemId") Long itemId,
                                                  HttpServletRequest request) throws IOException {

        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        UserItem userItem = userItemService.doLike(userId, itemId);

//        User receiveUser = userItem.getItem().getUser();
//        Notification userItemNotification = new Notification(receiveUser, userItem);
//        notificationService.sendMessage(userItemNotification);

        return OK(new UserItemResponse(userItem));
    }

    @DeleteMapping("/v1/items/{itemId}/like")
    @ApiOperation("좋아요 취소 요청 API")
    public ApiResult cancleLikeItem(@PathVariable("itemId") Long itemId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.cancelLikeItem(userId, itemId));
    }

    @GetMapping("/v1/items/like")
    @ApiOperation("좋아요한 아이템 목록 보기 API")
    public ApiResult<List<ItemDto>> getLikeItems(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.getLikeItemsOfUser(userId));
    }

    @GetMapping("/v2/items/like")
    @ApiOperation("좋아요한 아이템 목록 보기 API V2")
    public ApiResult<List<ItemSummaryDto>> getLikeItemsV2(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(userItemService.getLikeItemsOfUserV2(userId));
    }
}

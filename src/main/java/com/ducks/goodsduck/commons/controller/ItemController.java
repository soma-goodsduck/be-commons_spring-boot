package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.CategoryItemDto;
import com.ducks.goodsduck.commons.model.dto.UserItemResponse;
import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.dto.GradeStatusDto;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserItem;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.repository.CategoryItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.service.UserItemService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
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
    private final UserRepository userRepository;
    private final CategoryItemRepository categoryItemRepository;

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
    public ApiResult<ItemDetailResponse> showItemDetail(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {

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
    @ApiOperation(value = "아이템 수정")
    @PutMapping("/v1/items/{itemId}")
    public ApiResult<Long> editItem(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto) throws JsonProcessingException {
        ItemUpdateRequest itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequest.class);
        return OK(itemService.edit(itemId, itemUpdateRequest));
    }

    // HINT : 수정, 삭제는 itemDetail 불러올 때 이미 권한 여부 체크
    @NoCheckJwt
    @ApiOperation(value = "아이템 수정 V2")
    @PutMapping("/v2/items/{itemId}")
    public ApiResult<Long> editItemV2(@PathVariable("itemId") Long itemId,
                                      @RequestParam String stringItemDto,
                                      @RequestParam(required = false) List<MultipartFile> multipartFiles) throws JsonProcessingException {
        ItemUpdateRequestV2 itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequestV2.class);
        return OK(itemService.editV2(itemId, itemUpdateRequest, multipartFiles));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 삭제")
    @DeleteMapping("/v1/items/{itemId}")
    public ApiResult<Long> deleteItem(@PathVariable("itemId") Long itemId) {
        return OK(itemService.delete(itemId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 in 홈")
    @GetMapping("/v1/items")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> getItems(@RequestParam Integer pageNumber, @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.getItemList(pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.getItemList(userId, pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 in 홈 (V2_쿼리 성능 보완 및 검색 기능 추가)")
    @GetMapping("/v2/items")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> getItems(@RequestHeader("jwt") String jwt,
                                                                    @RequestParam("pageNumber") Integer pageNumber,
                                                                    @RequestParam(value = "keyword", required = false) String keyword) {
        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.getItemListV2(pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.getItemListUserV2(userId, pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items")
    @Transactional
    public ItemHomeResponseResult<List<ItemHomeResponse>> getItems(@RequestParam("itemId") Long itemId,
                                                                   @RequestHeader("jwt") String jwt) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;
        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = itemService.getItemListV3(itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = itemService.getItemListV3(userId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 검색 (회원/비회원)")
    @GetMapping("/v1/items/search")
    @Transactional
    public ItemHomeResponseResult<List<ItemHomeResponse>> getSearchedItems(@RequestParam("keyword") String keyword,
                                                                           @RequestParam("itemId") Long itemId,
                                                                           @RequestHeader("jwt") String jwt) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;
        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = itemService.getSearchedItemListForGuest(keyword, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = itemService.getSearchedItemListForUser(keyword, userId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹 필터링 in 홈")
    @GetMapping("/v1/items/filter")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> filterItemWithIdolGroup(@RequestParam("idolGroup") Long idolGroupId,
                                                                                   @RequestParam Integer pageNumber,
                                                                                   @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 아이돌 필터링
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.filterByIdolGroup(idolGroupId, pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 아이돌 필터링
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.filterByIdolGroup(userId, idolGroupId, pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹 필터링 in 홈 (V2_쿼리 성능 보완 및 검색 기능 추가)")
    @GetMapping("/v2/items/filter")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> filterItemWithIdolGroupV2(@RequestParam("idolGroup") Long idolGroupId,
                                                                                     @RequestParam Integer pageNumber,
                                                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                                                     @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 아이돌 필터링
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.filterByIdolGroupV2(idolGroupId, pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 아이돌 필터링
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.filterByIdolGroupV2(userId, idolGroupId, pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹 필터링 in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items/filter")
    @Transactional
    public ItemHomeResponseResult<List<ItemHomeResponse>> filterItemWithIdolGroupV3(@RequestParam("idolGroup") Long idolGroupId,
                                                                                    @RequestParam ("itemId") Long itemId,
                                                                                    @RequestHeader("jwt") String jwt) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;
        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 아이돌 필터링
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = itemService.filterByIdolGroupV3(idolGroupId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 아이돌 필터링
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = itemService.filterByIdolGroupV3(userId, idolGroupId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹=멤버, 거래타입, 카테고리, 상태, 가격대 필터링 in 홈")
    @GetMapping("/v1/items/filters")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> filterItemWithAll(@RequestParam(value = "idolMember", required = false) List<Long> idolMembersId,
                                                                             @RequestParam(value = "tradeType", required = false) TradeType tradeType,
                                                                             @RequestParam(value = "category", required = false) Long categoryItemId,
                                                                             @RequestParam(value = "gradeStatus", required = false) GradeStatus gradeStatus,
                                                                             @RequestParam(value = "minPrice", required = false) Long minPrice,
                                                                             @RequestParam(value = "maxPrice", required = false) Long maxPrice,
                                                                             @RequestParam Integer pageNumber,
                                                                             @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 모든 필터링
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.filterByAll(new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice), pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 모든 필터링
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.filterByAll(userId, new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice), pageNumber);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹=멤버, 거래타입, 카테고리, 상태, 가격대 필터링 in 홈 (V2_쿼리 성능 보완 및 검색 기능 추가)")
    @GetMapping("/v2/items/filters")
    @Transactional
    public ItemHomeResponseResult<Slice<ItemHomeResponse>> filterItemWithAllV2(@RequestParam(value = "idolMember", required = false) List<Long> idolMembersId,
                                                                               @RequestParam(value = "tradeType", required = false) TradeType tradeType,
                                                                               @RequestParam(value = "category", required = false) Long categoryItemId,
                                                                               @RequestParam(value = "gradeStatus", required = false) GradeStatus gradeStatus,
                                                                               @RequestParam(value = "minPrice", required = false) Long minPrice,
                                                                               @RequestParam(value = "maxPrice", required = false) Long maxPrice,
                                                                               @RequestParam(value = "keyword", required = false) String keyword,
                                                                               @RequestParam Integer pageNumber,
                                                                               @RequestHeader("jwt") String jwt) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 모든 필터링
        if(userId.equals(-1L)) {
            Slice<ItemHomeResponse> itemList = itemService.filterByAllV2(new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice),
                    pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 모든 필터링
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemHomeResponse> itemList = itemService.filterByAllV2(userId, new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice),
                    pageNumber, keyword);
            return ItemHomeResponseResult.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 + 아이돌 그룹=멤버, 거래타입, 카테고리, 상태, 가격대 필터링 in 홈 (V3 NoOffSet)")
    @GetMapping("/v3/items/filters")
    @Transactional
    public ItemHomeResponseResult<List<ItemHomeResponse>> filterItemWithAllV3(@RequestParam(value = "idolMember", required = false) List<Long> idolMembersId,
                                                                              @RequestParam(value = "tradeType", required = false) TradeType tradeType,
                                                                              @RequestParam(value = "category", required = false) Long categoryItemId,
                                                                              @RequestParam(value = "gradeStatus", required = false) GradeStatus gradeStatus,
                                                                              @RequestParam(value = "minPrice", required = false) Long minPrice,
                                                                              @RequestParam(value = "maxPrice", required = false) Long maxPrice,
                                                                              @RequestParam("itemId") Long itemId, @RequestHeader("jwt") String jwt) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;
        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈 + 모든 필터링
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = itemService.filterByAllV3(
                    new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice), itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 모든 필터링
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = itemService.filterByAllV3(userId,
                    new ItemFilterDto(idolMembersId, tradeType, categoryItemId, gradeStatus, minPrice, maxPrice), itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return ItemHomeResponseResult.OK(hasNext, new ItemDetailResponseUser(user), itemList);
        }
    }

    @ApiOperation(value = "카테고리 리스트 불러오기 in 아이템 등록")
    @GetMapping("/v1/items/category")
    @Transactional
    public ApiResult<List<CategoryItemDto>> getCategoryItem() {
        return OK(categoryItemRepository.findAll().stream()
                .map(categoryItem -> new CategoryItemDto(categoryItem))
                .collect(Collectors.toList()));
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
        User receiveUser = userItem.getItem().getUser();
        Notification userItemNotification = new Notification(receiveUser, userItem);

        notificationService.sendMessage(userItemNotification);

        return OK(new UserItemResponse(userItem));
    }

    @DeleteMapping("/v1/items/{itemId}/like")
    @ApiOperation("좋아요 취소 요청 API")
    public ApiResult cancleLikeItem(@PathVariable("itemId") Long itemId,
                                    HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
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

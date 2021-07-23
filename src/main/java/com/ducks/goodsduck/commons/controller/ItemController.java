package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.CategoryItemDto;
import com.ducks.goodsduck.commons.model.dto.GradeStatusDto;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.entity.Image;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.repository.CategoryItemRepository;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "아이템 CRUD APIs")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final CustomJwtService jwtService;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryItemRepository categoryItemRepository;

    @ApiOperation(value = "아이템 등록")
    @PostMapping("/item/new")
    public ApiResult<Long> uploadItem(@RequestParam String stringItemDto,
                           @RequestParam List<MultipartFile> multipartFiles,
                           HttpServletRequest request) throws IOException {

        ItemUploadRequest itemUploadRequest = new ObjectMapper().readValue(stringItemDto, ItemUploadRequest.class);
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);

        return OK(itemService.upload(itemUploadRequest, multipartFiles, userId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 상세보기")
    @GetMapping("/item/{itemId}")
    public ApiResult<ItemDetailResponse> showItemDetail(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {
        Long userId = userService.checkLoginStatus(jwt);
        return OK(itemService.showDetailWithLike(userId, itemId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 거래글의 글쓴이 여부 확인")
    @GetMapping("/item/edit/{itemId}")
    public ApiResult<Long> confirmWriter(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {
        Long userId = userService.checkLoginStatus(jwt);
        return OK(itemService.isWriter(userId, itemId));
    }

    @ApiOperation(value = "아이템 수정")
    @PutMapping("/item/edit/{itemId}")
    public ApiResult<Long> editItem(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto) throws JsonProcessingException {

        ItemUpdateRequest itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequest.class);
        return OK(itemService.edit(itemId, itemUpdateRequest));
    }

    @ApiOperation(value = "아이템 삭제")
    @DeleteMapping("/item/{itemId}")
    public ApiResult<Long> deleteItem(@PathVariable("itemId") Long itemId) {
        return OK(itemService.delete(itemId));
    }

    // 태호
//    @NoCheckJwt
//    @ApiOperation(value = "아이템 리스트 가져오기 in Home")
//    @GetMapping("/items")
//    @Transactional
//    public ApiResult<Page<ItemDetailResponse>> getItems(@RequestHeader("jwt") String jwt,
//                                                        @RequestParam("pageNumber") Integer pageNumber,
//                                                        @RequestParam("pageSize") Integer pageSize) {
//        Jws<Claims> claims = jwtService.getClaims(jwt);
//        Long userId = Long.valueOf(String.valueOf((claims.getBody().get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS))));
//        return OK(itemService.getItemList(userId, pageNumber, pageSize));
//    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 리스트 가져오기 in Home")
    @GetMapping("/items")
    @Transactional
    public ItemDetailResponseFinal<Slice<ItemDetailResponse>> getItems(@RequestHeader("jwt") String jwt,
                                                                       @RequestParam("pageNumber") Integer pageNumber) {

        Long userId = userService.checkLoginStatus(jwt);

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            Slice<ItemDetailResponse> itemList = itemService.getItemList(pageNumber);
            return ItemDetailResponseFinal.OK(itemList.hasNext(), null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            Slice<ItemDetailResponse> itemList = itemService.getItemListUser(userId, pageNumber);
            return ItemDetailResponseFinal.OK(itemList.hasNext(), new ItemDetailResponseUser(user), itemList);
        }
    }

    @ApiOperation(value = "카테고리 리스트 불러오기 in 아이템 등록")
    @GetMapping("/item/category")
    @Transactional
    public ApiResult<List<CategoryItemDto>> getCategoryItem() {
        return OK(categoryItemRepository.findAll().stream()
                .map(categoryItem -> new CategoryItemDto(categoryItem))
                .collect(Collectors.toList()));
    }

    @NoCheckJwt
    @ApiOperation(value = "제품상태 리스트 불러오기 in 아이템 등록")
    @GetMapping("/item/gradestatus")
    public ApiResult<List<GradeStatusDto>> getGradeStatusItem() {
        return OK(Arrays.asList(GradeStatus.values())
                .stream()
                .map(gradeStatus -> new GradeStatusDto(gradeStatus))
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "마이페이지의 아이템 거래내역 불러오기 API")
    @GetMapping("/mypage/item")
    public ApiResult<List<ItemSummaryDto>> getMyItemList(HttpServletRequest request, @RequestParam("tradeStatus") List<String> tradeStatusList) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        List<TradeStatus> statusList;
        try {
            statusList = tradeStatusList
                            .stream()
                            .map(tradeStatus -> TradeStatus.valueOf(tradeStatus.toUpperCase()))
                            .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.debug("Exception occurred in parsing tradeStatus: {}", e.getMessage(), e);
            throw new IllegalArgumentException("There is no tradeStatus inserted");
        }

        return OK(itemService.findMyItem(userId, statusList)
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    Image image = tuple.get(1, Image.class);
                    return ItemSummaryDto.of(item, image);
                })
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "아이템 거래 상태 변경 API")
    @PatchMapping("/item/{item_id}/tradeStatus")
    public ApiResult updateMyItemTradeStatus(HttpServletRequest request, @PathVariable("item_id") Long item_id, ItemTradeStatusUpdateRequest tradeStatus) {
        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        TradeStatus status;

        try {
            status = TradeStatus.valueOf(tradeStatus.getTradeStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Exception occurred in parsing tradeStatus: {}", e.getMessage(), e);
            throw new IllegalArgumentException("There is no tradeStatus inserted");
        }

        return OK(itemService.updateTradeStatus(userId, item_id, status));
    }

    // 경원 (보류)
//    @NoCheckJwt
//    @ApiOperation(value = "아이템 리스트 가져오기 in Home")
//    @GetMapping("/items")
//    @Transactional
//    public ApiResult<slice<ItemDetailResponse>> getItems(@RequestHeader("jwt") String jwt,
//                                                         @RequestParam("pageNumber") Integer pageNumber) {
//
//        Long userId = userService.checkLoginStatus(jwt);
//        userId = -1L;
//
//        // HINT : 비회원에게 보여줄 홈
////        if(userId.equals(-1L)) {
//            Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE, Sort.by("createdAt").descending());
//            return OK(itemRepository.findAll(pageable).map(item -> new ItemDetailResponse(item)));
////        }
////        // HINT : 회원에게 보여줄 홈
////        else {
////            return OK(itemService.getItemListUser(userId, pageNumber));
////        }
//    }
}

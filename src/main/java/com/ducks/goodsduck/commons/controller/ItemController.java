package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.CategoryItemDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserIdolGroup;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.repository.CategoryItemRepository;
import com.ducks.goodsduck.commons.repository.ImageRepository;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "아이템 CRUD API")
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
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));
        return OK(itemService.showDetailWithLike(userId, itemId));
    }

    @NoCheckJwt
    @ApiOperation(value = "아이템 거래글의 글쓴이 여부 확인")
    @GetMapping("/item/edit/{itemId}")
    public ApiResult<Long> confirmWriter(@PathVariable("itemId") Long itemId, HttpServletRequest request) {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
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

    // TODO : 좋아하는 아이돌 필터 추가
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
    public ApiResult<Slice<ItemDetailResponse>> getItems(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
                                                    /**, @RequestHeader("jwt") String jwt**/) {

//        Long userId = userService.checkLoginStatus(jwt);
        // 비회원에게 보여줄 홈페이지
//        if(userId.equals(-1)) {
            return OK(itemRepository.findAll(pageable).map(item -> new ItemDetailResponse(item)));
//        }
        // TODO : querydsl where 적용 + userService.updateLastLoginAt(userId) 적용;
//        else {
//            User user = userRepository.findById(userId).get();
//            List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();
//        }
    }

    @ApiOperation(value = "카테고리 리스트 불러오기 in 아이템 등록")
    @GetMapping("/item/category")
    @Transactional
    public List<CategoryItemDto> getCategoryItem() {
        return categoryItemRepository.findAll().stream()
                .map(categoryItem -> new CategoryItemDto(categoryItem))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "제품상태 리스트 불러오기 in 아이템 등록")
    @GetMapping("/item/gradestatus")
    public EnumSet<GradeStatus> getGradeStatusItem() { return EnumSet.allOf(GradeStatus.class); }
}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.repository.ImageRepository;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.service.ItemService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CustomJwtService jwtService;
    private final ImageUploadService imageUploadService;

    private final ItemRepository itemRepository;
    private final ImageRepository imageRepository;

    @PostMapping("/item/new")
    public ApiResult<Long> uploadItem(@RequestHeader("jwt") String jwt,
                                     @RequestParam String stringItemDto,
                                     @RequestParam List<MultipartFile> multipartFiles) throws IOException {

        /** String -> 클래스 객체 변환 **/
        ItemUploadRequest itemUploadRequest = new ObjectMapper().readValue(stringItemDto, ItemUploadRequest.class);

        /** Jwt에서 UserId 추출 **/
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));

        return OK(itemService.upload(itemUploadRequest, multipartFiles, userId));
    }

    @GetMapping("/item/{itemId}")
    public ApiResult<ItemDetailResponse> showItemDetail(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));
        return OK(itemService.showDetailWithLike(userId, itemId));
    }

    @ApiOperation(value = "아이템 글쓴이인지 여부 확인")
    @GetMapping("/item/edit/{itemId}")
    public ApiResult<Long> confirmWriter(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {

        /** Jwt에서 UserId 추출 **/
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));

        return OK(itemService.isWriter(userId, itemId));
    }
    
    @PutMapping("/item/edit/{itemId}")
    public ApiResult<Long> editItem(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto) throws JsonProcessingException {

        /** String -> 클래스 객체 변환 **/
        ItemUpdateRequest itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequest.class);
        return OK(itemService.edit(itemId, itemUpdateRequest));
    }

    // TODO : 아이템 삭제 구현
    @DeleteMapping("/item/{itemId}")
    public ApiResult<Long> deleteItem(@PathVariable("itemId") Long itemId) {

        return OK(Long.valueOf(1));
    }

    // TODO : 좋아하는 아이돌 필터 추가
    @ApiOperation(value = "아이템 리스트 가져오기 (Srot 최신순 적용 O, 좋아하는 아이돌 필터링 적용 X)")
    @GetMapping("/items")
    @Transactional
    public ApiResult<Page<ItemDetailResponse>> getItems(@RequestHeader("jwt") String jwt,
                                                        @RequestParam("pageNumber") Integer pageNumber,
                                                        @RequestParam("pageSize") Integer pageSize
    ) {
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS))));
        return OK(itemService.getItemList(userId, pageNumber, pageSize));
    }

    // TODO : 좋아하는 아이돌 필터 추가
//    @NoCheckJwt
//    @ApiOperation(value = "아이템 리스트 가져오기 (Sort 적용 X)")
//    @GetMapping("/items")
//    @Transactional
//    public List<ItemDetailResponse> getItems(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        List<Item> all = itemRepository.findAll();
//        List<ItemDetailResponse> items = all.stream().map(item -> new ItemDetailResponse(item)).collect(Collectors.toList());
//        return items;
//    }

//    @PostMapping("/item")
//    public testDto test() {
//
//        testDto testDto = new testDto();
//
//        ImageDto imageDto1 = new ImageDto("originName", "uploadName");
//        ImageDto imageDto2 = new ImageDto("originName", "uploadName");
//
//        testDto.setName("goods");
//        testDto.getA().add(imageDto1);
//        testDto.getA().add(imageDto2);
//
//        return testDto;
//    }

//    @PostMapping("/item")
//    public ItemDetailResponse uploadItem(@RequestBody ItemUploadRequest itemUploadRequest) {
//        return new ItemDetailResponse(
//                itemService.upload(itemUploadRequest)
//        );
//    }
//
//    @GetMapping("/item/{itemId}")
//    public ItemDetailResponse getDetails(@PathVariable("itemId") Long itemId) {
//        return itemService.getDetails(itemId)
//                .map(item -> new ItemDetailResponse(item))
//                .orElseGet(() -> new ItemDetailResponse());
//    }

    //    @Transactional
//    @PostMapping("/item/new/2")
//    public List<ImageDto> uploadItem(@ModelAttribute ItemUploadRequest itemUploadRequest) throws IOException {
//
//        System.out.println(itemUploadRequest.getUser());
//        System.out.println(itemUploadRequest.getMultipartFiles().get(0).getOriginalFilename());
//        System.out.println(itemUploadRequest.getMultipartFiles().get(1).getOriginalFilename());
//        System.out.println(itemUploadRequest.getMultipartFiles().get(2).getOriginalFilename());
//
//        List<MultipartFile> multipartFiles = itemUploadRequest.getMultipartFiles();
//        List<ImageDto> imageDtos = imageService.uploadImages(multipartFiles);
//
//
//        Item goods = new Item("goods", 10000);
//        itemRepository.save(goods);
//
//        for (ImageDto imageDto : imageDtos) {
//
//            Image image = new Image(imageDto.getOriginName(), imageDto.getUploadName());
//            goods.addImage(image);
//            imageRepository.save(image);
//        }
//
//        return imageDtos;
//    }
}

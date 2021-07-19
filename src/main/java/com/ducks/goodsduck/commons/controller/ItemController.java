package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.TestDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.item.ItemDto;
import com.ducks.goodsduck.commons.model.dto.item.ItemUpdateRequest;
import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.repository.ImageRepository;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.service.ItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    public Long uploadItem(@RequestParam String stringItemDto,
                           @RequestParam List<MultipartFile> multipartFiles,
                           @RequestHeader("jwt") String jwt) throws IOException {

        /** String -> 클래스 객체 변환 **/
        ItemUploadRequest itemUploadRequest = new ObjectMapper().readValue(stringItemDto, ItemUploadRequest.class);

        /** Jwt에서 UserId 추출 **/
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));

        System.out.println(multipartFiles.get(0));
        System.out.println(multipartFiles.get(1));

        return itemService.upload(itemUploadRequest, multipartFiles, userId);
    }

    @GetMapping("/item/{itemId}")
    public ItemDetailResponse showItemDetail(@PathVariable("itemId") Long itemId) {
        return itemService.showDetail(itemId);
    }

    @ApiOperation(value = "아이템 글쓴이인지 여부 확인")
    @GetMapping("/item/edit/{itemId}")
    public Long confirmWriter(@RequestHeader("jwt") String jwt, @PathVariable("itemId") Long itemId) {

        /** Jwt에서 UserId 추출 **/
        Jws<Claims> claims = jwtService.getClaims(jwt);
        Long userId = Long.valueOf(String.valueOf((claims.getBody().get("userId"))));

        return itemService.isWriter(userId, itemId);
    }
    
    @PutMapping("/item/edit/{itemId}")
    public Long editItem(@PathVariable("itemId") Long itemId, @RequestParam String stringItemDto) throws JsonProcessingException {

        /** String -> 클래스 객체 변환 **/
        ItemUpdateRequest itemUpdateRequest = new ObjectMapper().readValue(stringItemDto, ItemUpdateRequest.class);

        return itemService.edit(itemId, itemUpdateRequest);
    }

    // TODO : 아이템 삭제 구현
    @DeleteMapping("/item/{itemId}")
    public Long deleteItem(@PathVariable("itemId") Long itemId) {

        return Long.valueOf(1);
    }

//    // TODO : 좋아하는 아이돌 필터 추가
//    @ApiOperation(value = "아이템 리스트 가져오기 in Home")
//    @GetMapping("/items")
//    public Page<ItemDetailResponse> getItems(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        return itemRepository.findAll(pageable).map(item -> new ItemDetailResponse(item));
//    }

    // TODO : 좋아하는 아이돌 필터 추가
    @ApiOperation(value = "아이템 리스트 가져오기 in Home")
    @GetMapping("/items")
    public Integer getItems(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        List<Item> all = itemRepository.findAll();

        List<ItemDetailResponse> collect = all.stream().map(item -> new ItemDetailResponse(item)).collect(Collectors.toList());

        for (ItemDetailResponse itemDetailResponse : collect) {
            System.out.println(itemDetailResponse.getUser().getNickName());
        }

        return 1;
    }

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

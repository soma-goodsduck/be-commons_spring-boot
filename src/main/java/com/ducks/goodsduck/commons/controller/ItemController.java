package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.repository.ImageRepository;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

        return itemService.uploadItem(itemUploadRequest, multipartFiles, userId);
    }

    // 상세페이지에 접근할때마다 조회수 1 증가 구현해야함
    @GetMapping("/item/{itemId}")
    public ItemDetailResponse showItemDetail(@PathVariable("itemId") Long itemId) {
        return itemService.showItemDetail(itemId);
    }
    
    @PutMapping("/item/edit/{itemId}")
    public String updateItem(@PathVariable("itemId") Long itemId) {
        return "ok";
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

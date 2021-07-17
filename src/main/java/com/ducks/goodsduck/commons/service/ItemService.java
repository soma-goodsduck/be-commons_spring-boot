package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.dto.ItemDetailResponse;
import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final IdolMemberRepository idolMemberRepository;
    private final CategoryRepository categoryRepository;

    private final ImageUploadService imageUploadService;

    public Long uploadItem(ItemUploadRequest itemUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            /** 이미지 업로드 처리 **/
            List<ImageDto> imageDtos = imageUploadService.uploadImages(multipartFiles);

            Item item = new Item(itemUploadRequest);

            /** Item-User 연관관계 삽입 **/
            User findUser = userRepository.findById(userId).get();
            item.setUser(findUser);

            /** Item-IdolMember 연관관계 삽입 **/
            IdolMember idolMember = idolMemberRepository.findById(itemUploadRequest.getIdolMember()).get();
            item.setIdolMember(idolMember);

            /** Item-Category 연관관계 삽입 **/
//            Category category = categoryRepository.findById(itemUploadRequest.getCategory()).get();
            Category category = categoryRepository.findByName(itemUploadRequest.getCategory());

            System.out.println("@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println(category.getId());
            System.out.println(category.getName());

            item.setCategory(category);

            itemRepository.save(item);

            /** Image-Item 연관관계 삽입 **/
            for (ImageDto imageDto : imageDtos) {
                Image image = new Image(imageDto.getOriginName(), imageDto.getUploadName(), imageDto.getUrl());
                item.addImage(image);
                imageRepository.save(image);
            }

            List<Image> images = item.getImages();

            for (Image image : images) {
                System.out.println(image.getUrl());
            }

            return item.getId();
        } catch (Exception e) {
            return Long.valueOf(-1);
        }
    }

    public ItemDetailResponse showItemDetail(Long itemId) {

        Item item = itemRepository.findById(itemId).get();
        ItemDetailResponse itemDetailResponse =  new ItemDetailResponse(item);

        System.out.println(itemDetailResponse);

        return itemDetailResponse;
    }

    public String updateItem(Long itemId) {

        Item item = itemRepository.findById(itemId).get();


        return "ok";
    }

    public Optional<Item> getDetails(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public Item upload(ItemUploadRequest itemUploadRequest) {
        return itemRepository.save(new Item(itemUploadRequest));
    }
}

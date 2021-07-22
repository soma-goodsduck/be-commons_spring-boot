package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemRepositoryCustom itemRepositoryCustom;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final IdolMemberRepository idolMemberRepository;
    private final CategoryItemRepository categoryItemRepository;

    private final ImageUploadService imageUploadService;

    public Long upload(ItemUploadRequest itemUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

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
            CategoryItem categoryItem = categoryItemRepository.findByName(itemUploadRequest.getCategory());
            item.setCategoryItem(categoryItem);

            itemRepository.save(item);

            /** Image-Item 연관관계 삽입 **/
            for (ImageDto imageDto : imageDtos) {
                Image image = new Image(imageDto.getOriginName(), imageDto.getUploadName(), imageDto.getUrl());
                item.addImage(image);
                imageRepository.save(image);
            }

            List<Image> images = item.getImages();

            return item.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public ItemDetailResponse showDetail(Long itemId) {

        Item item = itemRepository.findById(itemId).get();
        item.increaseView();

        return new ItemDetailResponse(item);
    }

    public ItemDetailResponse showDetailWithLike(Long userId, Long itemId) {
        Tuple itemTupleWithUserItem = itemRepositoryCustom.findByIdWithUserItem(userId, itemId);
        Item item = itemTupleWithUserItem.get(0, Item.class);
        item.increaseView();
        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);

        if (itemTupleWithUserItem.get(1, long.class) > 0L) {
            itemDetailResponse.likesOfMe();
        };

        return itemDetailResponse;
    }

    public Long edit(Long itemId, ItemUpdateRequest itemUpdateRequest) {

        try {
            Item item = itemRepository.findById(itemId).get();
            item.setName(itemUpdateRequest.getName());
            item.setDescription(itemUpdateRequest.getDescription());
            item.setPrice(itemUpdateRequest.getPrice());
            item.setTradeType(itemUpdateRequest.getTradeType());
            item.setGradeStatus(itemUpdateRequest.getGradeStatus());

            IdolMember idolMember = idolMemberRepository.findById(itemUpdateRequest.getIdolMember()).get();
            item.setIdolMember(idolMember);

            return item.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long delete(Long itemId) {

        try {
            Item deleteItem = itemRepository.findById(itemId).get();
            List<Image> deleteImages = imageRepository.findAllByItemId(itemId);

            User user = deleteItem.getUser();
            List<Item> userItems = user.getItems();

            for (Image deleteImage : deleteImages) {
                imageRepository.delete(deleteImage);
            }

            for (Item userItem : userItems) {
                if(userItem.getId().equals(deleteItem.getId())) {
                    userItems.remove(userItem);
                    break;
                }
            }

            itemRepository.delete(deleteItem);
            
            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long isWriter(Long userId, Long itemId) {

        Long findUserId = itemRepository.findById(itemId).get().getUser().getId();

        if(userId.equals(findUserId)) {
            return Long.valueOf(1);
        } else {
            return Long.valueOf(-1);
        }
    }

    public Optional<Item> getDetails(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public Item upload(ItemUploadRequest itemUploadRequest) {
        return itemRepository.save(new Item(itemUploadRequest));
    }

    // HINT : 태호
    public Page<ItemDetailResponse> getItemListUser(Long userId, Integer pageNumber, Integer pageSize) {

        String property = "createdAt";

        Sort.Order createdAtDesc = Sort.Order.desc(property);
        Sort sort = Sort.sort(Item.class).by(createdAtDesc);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllWithUserItem(userId, pageable);
        List<ItemDetailResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    long count = tuple.get(1, long.class);

                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
                    if (count > 0L) {
                        itemDetailResponse.likesOfMe();
                    }
                    return itemDetailResponse;
                })
                .collect(Collectors.toList());

        long count = tupleToList.size();
        int start = pageNumber * pageSize;
        int maxCount = (pageNumber+1) * pageSize;
        int end = maxCount > count ? (int) count : maxCount;

        return new PageImpl(tupleToList.subList(start, end), pageable, count);
    }

    // HINT : 비회원용 (경원)
    public Slice<ItemDetailResponse> getItemList(Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ItemDetailResponse> itemToList =  items
                .stream()
                .map(item -> new ItemDetailResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // HINT : 회원용 (경원)
    public Slice<ItemDetailResponse> getItemListUser(Long userId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllWithUserItemIdolGroup(userId, userIdolGroups, pageable);
        List<ItemDetailResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
                    if(userItem != null && userItem.getUser().getId().equals(userId)) {
                        itemDetailResponse.likesOfMe();
                    }
//                    long count = tuple.get(1, long.class);
//
//                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
//                    if (count > 0L) {
//                        itemDetailResponse.likesOfMe();
//                    }

                    return itemDetailResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    public static <T> Slice<T> toSlice(final List<T> contents, final Pageable pageable) {
        final boolean hasNext = isContentSizeGreaterThanPageSize(contents, pageable);
        return new SliceImpl<>(hasNext ? subListLastContent(contents, pageable) : contents, pageable, hasNext);
    }

    private static <T> boolean isContentSizeGreaterThanPageSize(final List<T> content, final Pageable pageable) {
        return pageable.isPaged() && content.size() > pageable.getPageSize();
    }

    private static <T> List<T> subListLastContent(final List<T> content, final Pageable pageable) {
        return content.subList(0, pageable.getPageSize());
    }
}

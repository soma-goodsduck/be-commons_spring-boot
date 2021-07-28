package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.Tuple;
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

import static com.ducks.goodsduck.commons.model.enums.TradeStatus.*;

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
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;

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
                Image image = new Image(imageDto);
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

        Tuple itemTupleWithUserItem;

        // HINT: 비회원인 경우
        if (userId.equals(-1L)) {
            itemTupleWithUserItem = itemRepositoryCustom.findByItemId(itemId);
            Item item = itemTupleWithUserItem.get(0, Item.class);
            item.increaseView();
            return new ItemDetailResponse(item);
        }

        itemTupleWithUserItem = itemRepositoryCustom.findByIdWithUserItem(userId, itemId);
        Item item = itemTupleWithUserItem.get(0, Item.class);
        item.increaseView();

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
        itemDetailResponse.setUserId(userId);

        if (itemTupleWithUserItem.get(1, long.class) > 0L) {
            itemDetailResponse.likesOfMe();
        }

        if (item.getUser().getId().equals(userId)) {
            itemDetailResponse.myItem();
            return itemDetailResponse;
        }

        // HINT: 아이템 주인이 아닌 경우, 가격 제안 정보 여부 조회
        List<PricePropose> priceProposes = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);

        itemDetailResponse.addProposedList(priceProposes);

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

    public Long isItemOwner(Long userId, Long itemId) {

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

    // FEAT : 비회원용 홈
    public Slice<ItemDetailResponse> getItemList(Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAll(pageable);
        List<ItemDetailResponse> itemToList =  items
                .stream()
                .map(item -> new ItemDetailResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT : 회원용 홈
    public Slice<ItemDetailResponse> getItemList(Long userId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItem(userId, userIdolGroups, pageable);

        List<ItemDetailResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
                    if(userItem != null) {
                        itemDetailResponse.likesOfMe();
                    }

                    return itemDetailResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 비회원용 홈 필터링 (아이돌 그룹)
    public Slice<ItemDetailResponse> filterByIdolGroup(Long idolGroupId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByIdolGroup(idolGroupId, pageable);

        List<ItemDetailResponse> itemToList =  items
                .stream()
                .map(item -> new ItemDetailResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }
    
    // FEAT : 회원용 홈 필터링 (아이돌그룹)
    public Slice<ItemDetailResponse> filterByIdolGroup(Long userId, Long idolGroupId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItem(userId, idolGroupId, pageable);

        List<ItemDetailResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
                    if (userItem != null) {
                        itemDetailResponse.likesOfMe();
                    }

                    return itemDetailResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT: 비회원용 홈 필터링 (ALL)
    public Slice<ItemDetailResponse> filterByAll(ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByFilterWithUserItem(itemFilterDto, pageable);

        List<ItemDetailResponse> itemToList =  items
                .stream()
                .map(item -> new ItemDetailResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }


    // FEAT : 회원용 홈 필터링 (ALL)
    public Slice<ItemDetailResponse> filterByAll(Long userId, ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterWithUserItem(userId, itemFilterDto, pageable);

        List<ItemDetailResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
                    if (userItem != null) {
                        itemDetailResponse.likesOfMe();
                    }

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

    public List<Tuple> findMyItem(Long userId, TradeStatus status) {
        return itemRepositoryCustom.findAllByUserIdAndTradeStatus(userId, status);
    }

    public boolean updateTradeStatus(Long userId, Long itemId, TradeStatus status) {
        Optional<Item> findItemOpt = itemRepository.findById(itemId);

        Item findItem = findItemOpt.orElseThrow(() -> {
            throw new IllegalArgumentException("Not founded item.");
        });

        if (findItem.getTradeStatus().equals(COMPLETE)) {
            throw new IllegalArgumentException("Already completed trade item.");
        }

        TradeType tradeType = findItem.getTradeType();

        switch (status) {
            case BUYING:
                if (tradeType.equals(TradeType.SELL)) throw new IllegalArgumentException("This item's tradeType is SELLING");
                break;

            case SELLING:
                if (tradeType.equals(TradeType.BUY)) throw new IllegalArgumentException("This item's tradeType is BUYING");
                break;
        }

        return itemRepositoryCustom.updateTradeStatus(itemId, status) > 0 ? true : false;
    }
}

package com.ducks.goodsduck.commons.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.ImageType;
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
import java.util.Iterator;
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
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final IdolMemberRepository idolMemberRepository;
    private final CategoryItemRepository categoryItemRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;

    private final ImageUploadService imageUploadService;

    public Long upload(ItemUploadRequest itemUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            /** 이미지 업로드 처리 **/
            List<ImageDto> imageDtos = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM);

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

            return item.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public ItemDetailResponse showDetail(Long itemId) {

        Item item = itemRepository.findById(itemId).get();
        item.increaseView();

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);

        List<PricePropose> priceProposes = priceProposeRepositoryCustom.findAllByItemId(itemId);
        itemDetailResponse.addProposedList(priceProposes);

        return itemDetailResponse;
    }

    public ItemDetailResponse showDetailWithLike(Long userId, Long itemId) {

        Tuple itemTupleWithUserItem = itemRepositoryCustom.findByIdWithUserItem(userId, itemId);
        Item item = itemTupleWithUserItem.get(0, Item.class);
        item.increaseView();

        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new NotFoundException("User not founded.");
                });

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
        itemDetailResponse.setLoginUser(new UserSimpleDto(loginUser));

        if (itemTupleWithUserItem.get(1, long.class) > 0L) {
            itemDetailResponse.likesOfMe();
        }

        if (item.getUser().getId().equals(userId)) {
            itemDetailResponse.myItem();
            return itemDetailResponse;
        }

        // HINT: 가격 제안 정보 조회
        List<PricePropose> priceProposes = priceProposeRepositoryCustom.findAllByItemId(itemId);
        itemDetailResponse.addProposedList(priceProposes);
        
        // HINT: 가격 제안 유무 체크
        PricePropose pricePropose = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);
        if(pricePropose != null) {
            itemDetailResponse.addMyPricePropose(pricePropose);
        }

        // HINT: 채팅방 정보
        Chat chat = userChatRepositoryCustom.findByUserIdAndItemId(userId, itemId);
        if(chat != null) {
            itemDetailResponse.setChatId(chat.getId());
        }

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
            List<Item> itemsOfUser = user.getItems();

            itemsOfUser.remove(deleteItem);
            imageRepository.deleteInBatch(deleteImages);
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

    // FEAT : 비회원용 홈 (V1)
    public Slice<ItemHomeResponse> getItemList(Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAll(pageable);
        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT : 비회원용 홈 (V2)
    public Slice<ItemHomeResponseV2> getItemListV2(Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllV2(pageable, keyword);
        List<ItemHomeResponseV2> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 비회원용 홈 (V3)
    public List<ItemHomeResponse> getItemListV3(Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllV3(itemId);
        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : 회원용 홈
    public Slice<ItemHomeResponse> getItemList(Long userId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItem(userId, userIdolGroups, pageable);

        List<ItemHomeResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 (V2)
    public Slice<ItemHomeResponseV2> getItemListUserV2(Long userId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItemV2(userId, userIdolGroups, pageable, keyword);

        List<ItemHomeResponseV2> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 (V3)
    public List<ItemHomeResponse> getItemListV3(Long userId, Long itemId) {

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItemV3(userId, userIdolGroups, itemId);

        List<ItemHomeResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return tupleToList;
    }


    // FEAT : 비회원용 홈 필터링 (아이돌 그룹)
    public Slice<ItemHomeResponse> filterByIdolGroup(Long idolGroupId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByIdolGroup(idolGroupId, pageable);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT : 비회원용 홈 필터링 (아이돌 그룹) V2
    public Slice<ItemHomeResponseV2> filterByIdolGroupV2(Long idolGroupId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupV2(idolGroupId, pageable, keyword);

        List<ItemHomeResponseV2> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 비회원용 홈 필터링 (아이돌 그룹) V3
    public List<ItemHomeResponse> filterByIdolGroupV3(Long idolGroupId, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByIdolGroupV3(idolGroupId, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : 회원용 홈 필터링 (아이돌그룹)
    public Slice<ItemHomeResponse> filterByIdolGroup(Long userId, Long idolGroupId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItem(userId, idolGroupId, pageable);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if (userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (아이돌그룹) V2
    public Slice<ItemHomeResponseV2> filterByIdolGroupV2(Long userId, Long idolGroupId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItemV2(userId, idolGroupId, pageable, keyword);

        List<ItemHomeResponseV2> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (아이돌그룹) V3
    public List<ItemHomeResponse> filterByIdolGroupV3(Long userId, Long idolGroupId, Long itemId) {

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItemV3(userId, idolGroupId, itemId);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if (userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return tupleToList;
    }

    // FEAT: 비회원용 홈 필터링 (ALL)
    public Slice<ItemHomeResponse> filterByAll(ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByFilterWithUserItem(itemFilterDto, pageable);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT: 비회원용 홈 필터링 (ALL) V2
    public Slice<ItemHomeResponseV2> filterByAllV2(ItemFilterDto itemFilterDto, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterWithUserItemV2(itemFilterDto, pageable, keyword);

        List<ItemHomeResponseV2> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT: 비회원용 홈 필터링 (ALL) V3
    public List<ItemHomeResponse> filterByAllV3(ItemFilterDto itemFilterDto, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByFilterWithUserItemV3(itemFilterDto, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : 회원용 홈 필터링 (ALL)
    public Slice<ItemHomeResponse> filterByAll(Long userId, ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterWithUserItem(userId, itemFilterDto, pageable);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if (userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (ALL) V2
    public Slice<ItemHomeResponseV2> filterByAllV2(Long userId, ItemFilterDto itemFilterDto, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterWithUserItemV2(userId, itemFilterDto, pageable, keyword);

        List<ItemHomeResponseV2> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponseV2 itemHomeResponse = new ItemHomeResponseV2(item);
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    itemHomeResponse.setImageUrl(image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (ALL) V3
    public List<ItemHomeResponse> filterByAllV3(Long userId, ItemFilterDto itemFilterDto, Long itemId) {

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterWithUserItemV3(userId, itemFilterDto, itemId);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item);
                    if (userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return tupleToList;
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

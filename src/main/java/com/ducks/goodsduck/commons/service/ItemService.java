package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationBadgeResponse;
import com.ducks.goodsduck.commons.model.dto.user.MypageResponse;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import com.ducks.goodsduck.commons.model.entity.report.ItemReport;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.ReportRepository.ItemReportRepository;
import com.ducks.goodsduck.commons.repository.category.ItemCategoryRepository;
import com.ducks.goodsduck.commons.repository.idol.IdolMemberRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepositoryCustom;
import com.ducks.goodsduck.commons.repository.image.ItemImageRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepositoryCustom;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;
import static com.ducks.goodsduck.commons.model.enums.TradeStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemRepositoryCustom itemRepositoryCustom;
    private final ImageRepository imageRepository;
    private final ImageRepositoryCustom imageRepositoryCustom;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final UserChatRepository userChatRepository;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final IdolMemberRepository idolMemberRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final UserItemRepository userItemRepository;
    private final UserItemRepositoryCustom userItemRepositoryCustom;
    private final PriceProposeRepository priceProposeRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final ItemImageRepository itemImageRepository;
    private final ItemReportRepository itemReportRepository;

    private final ImageUploadService imageUploadService;

    public Long upload(ItemUploadRequest itemUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        try {
            Item item = new Item(itemUploadRequest);

            /** Item-User 연관관계 삽입 **/
            User user = userRepository.findById(userId).get();
            item.setUser(user);

            /** Item-IdolMember 연관관계 삽입 **/
            IdolMember idolMember = idolMemberRepository.findById(itemUploadRequest.getIdolMember()).get();
            item.setIdolMember(idolMember);

            /** Item-Category 연관관계 삽입 **/
            ItemCategory itemCategory = itemCategoryRepository.findByName(itemUploadRequest.getCategory());
            item.setItemCategory(itemCategory);

            /** 이미지 업로드 처리 & Item-Image 연관관계 삽입 **/
            List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM, user.getNickName());
            for (Image image : images) {
                ItemImage itemImage = new ItemImage(image);
                item.addImage(itemImage);
                imageRepository.save(itemImage);
            }

            itemRepository.save(item);

            user.gainExp(10);

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

        User loginUser = userRepository.findById(userId).get();
        if(loginUser == null) {
            return showDetail(itemId);
        }

        Tuple itemTupleWithUserItem = itemRepositoryCustom.findByIdWithUserItem(userId, itemId);
        Item item = itemTupleWithUserItem.get(0, Item.class);
        item.increaseView();

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
        itemDetailResponse.setLoginUser(new UserSimpleDto(loginUser));

        if (itemTupleWithUserItem.get(1, long.class) > 0L) {
            itemDetailResponse.likesOfMe();
        }

        // 아이템 주인인 경우
        if (item.getUser().getId().equals(userId)) {
            // 받은 가격 제안 정보 조회
            List<PricePropose> priceProposes = priceProposeRepositoryCustom.findAllByItemId(itemId);
            itemDetailResponse.addProposedList(priceProposes);
            
            // 내 아이템 체크
            itemDetailResponse.myItem();
        } else {
            // 가격 제안 유무 체크
            PricePropose pricePropose = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);
            if(pricePropose != null) {
                itemDetailResponse.addMyPricePropose(pricePropose);
            }

            // 채팅방 정보
            Chat chat = userChatRepositoryCustom.findByUserIdAndItemId(userId, itemId);
            if(chat != null) {
                itemDetailResponse.setChatId(chat.getId());
            }
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
            ItemCategory itemCategory = itemCategoryRepository.findByName(itemUpdateRequest.getCategory());
            item.setItemCategory(itemCategory);

            return item.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long editV2(Long itemId, ItemUpdateRequestV2 itemUpdateRequest, List<MultipartFile> multipartFiles, Long userId) {

        // TODO : 빈리스트 올 경우 처리
        
        try {
            /**
             * 기존 아이템 정보 수정
             * 이름
             * 가격
             * 설명
             * 상태 (S, A, B, C)
             * 타입 (BUY, SELL)
             * 아이돌 멤버
             * 카테고리
             */
            Item item = itemRepository.findById(itemId).get();
            item.setName(itemUpdateRequest.getName());
            item.setDescription(itemUpdateRequest.getDescription());
            item.setPrice(itemUpdateRequest.getPrice());
            item.setTradeType(itemUpdateRequest.getTradeType());
            if(itemUpdateRequest.getTradeType().equals(TradeType.BUY)) {
                item.setTradeStatus(TradeStatus.BUYING);
            } else {
                item.setTradeStatus(TradeStatus.SELLING);
            }
            item.setGradeStatus(itemUpdateRequest.getGradeStatus());
            IdolMember idolMember = idolMemberRepository.findById(itemUpdateRequest.getIdolMember()).get();
            item.setIdolMember(idolMember);
            ItemCategory itemCategory = itemCategoryRepository.findByName(itemUpdateRequest.getCategory());
            item.setItemCategory(itemCategory);

            /**
             * 기존 이미지 수정 (Url)
             * case1. 기존 이미지 유지한 경우 -> 따로 변경할 필요없음
             * case2. 기존 이미지 전부 삭제한 경우 (null, empty)
             * case3. 기존 이미지 일부 삭제한 경우
             *
             * 새로운 이미지 수정 (파일)
             * case4. 새로운 이미지 추가하지 않은 경우 -> 따로 변경할 필요없음
             * case5. 새로운 이미지 추가한 경우
             */
            List<ItemImage> existImages = item.getImages();
            List<String> updateImageUrls = itemUpdateRequest.getImageUrls();

            // case2
            if(updateImageUrls.isEmpty()) {
                List<Image> deleteImages = new ArrayList<>();
                Iterator<ItemImage> iter = existImages.iterator();
                while(iter.hasNext()) {
                    Image existImage = iter.next();
                    deleteImages.add(existImage);
                    iter.remove();
                }

                imageRepository.deleteInBatch(deleteImages);
            }
            // case3
            else {
                List<Image> updateImages = imageRepositoryCustom.findByImageUrls(updateImageUrls);

                if(!(existImages.containsAll(updateImages) && updateImages.containsAll(existImages))) {

                    HashMap<Long, String> imageMap = new HashMap<>();
                    for (Image updateExistImage : updateImages) {
                        imageMap.put(updateExistImage.getId(), updateExistImage.getUrl());
                    }

                    List<Image> deleteImages = new ArrayList<>();
                    Iterator<ItemImage> iter = existImages.iterator();
                    while(iter.hasNext()) {
                        Image existImage = iter.next();
                        if(imageMap.get(existImage.getId()) == null) {
                            deleteImages.add(existImage);
                            iter.remove();
                        }
                    }

                    imageRepository.deleteInBatch(deleteImages);
                }
            }

            // case5
            if(multipartFiles != null) {
                User user = userRepository.findById(userId).get();
                List<Image> images = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM, user.getNickName());
                for (Image image : images) {
                    ItemImage itemImage = new ItemImage(image);
                    item.addImage(itemImage);
                    imageRepository.save(itemImage);
                }
            }

            return item.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long delete(Long itemId) {

        try {
            Item deleteItem = itemRepository.findById(itemId).get();

            // user's item 목록 삭제
            User user = deleteItem.getUser();
            List<Item> itemsOfUser = user.getItems();
            itemsOfUser.remove(deleteItem);

            // image 연관 삭제
            List<ItemImage> deleteImages = deleteItem.getImages();
            itemImageRepository.deleteInBatch(deleteImages);

            // pricePropose 연관 삭제
            List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(itemId);
            priceProposeRepository.deleteInBatch(deletePriceProposes);

            // userChat 연관 삭제
            List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(itemId);
            userChatRepository.deleteInBatch(deleteUserChats);

            // chat 삭제
            List<Chat> deleteChats = new ArrayList<>();
            for (UserChat deleteUserChat : deleteUserChats) {
                deleteChats.add(deleteUserChat.getChat());
            }
            chatRepository.deleteInBatch(deleteChats);

            // TODO : 0번 아이템 만든 후에 체크
//            Item replaceItem = itemRepository.findById(0L).get();

            // review 연관 삭제 (삭제용 데이터 0번 아이템으로 교체)
            List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(itemId);
            for (Review deleteItemOfReview : deleteItemOfReviews) {
                // TODO : 0번 아이템 만든 후에 체크
                deleteItemOfReview.setItem(null);
//                deleteItemOfReview.setItem(replaceItem);
            }

            // userItem 연관 삭제
            List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(itemId);
            userItemRepository.deleteInBatch(deleteUserItems);

            // item 삭제
            itemRepository.delete(deleteItem);

            return 1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Long deleteV2(Long itemId) {
        try {
            Item item = itemRepository.findById(itemId).get();
            item.setDeletedAt(LocalDateTime.now());
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
    public Slice<ItemHomeResponse> getItemListV2(Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllV2(pageable, keyword);
        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 비회원용 홈 (V3)
    public List<ItemHomeResponse> getItemListForAnonymousV3(Long itemId) {

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
    public Slice<ItemHomeResponse> getItemListUserV2(Long userId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItemV2(userId, userIdolGroups, pageable, keyword);

        List<ItemHomeResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 (V3)
    public List<ItemHomeResponse> getItemListForUserV3(Long userId, Long itemId) {

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
    public Slice<ItemHomeResponse> filterByIdolGroupV2(Long idolGroupId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupV2(idolGroupId, pageable, keyword);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 비회원용 홈 필터링 (아이돌 그룹) V3
    public List<ItemHomeResponse> getItemListFilterByIdolGroupForAnonoymousV3(Long idolGroupId, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByIdolGroupV3(idolGroupId, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : 회원용 홈 필터링 (아이돌그룹)
    public Slice<ItemHomeResponse> filterByIdolGroupV2(Long userId, Long idolGroupId, Integer pageNumber) {

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
    public Slice<ItemHomeResponse> filterByIdolGroupV2(Long userId, Long idolGroupId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItemV2(userId, idolGroupId, pageable, keyword);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (아이돌그룹) V3
    public List<ItemHomeResponse> getItemListFilterByIdolGroupForUserV3(Long userId, Long idolGroupId, Long itemId) {

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

        List<Item> items = itemRepositoryCustom.findAllByFilter(itemFilterDto, pageable);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT: 비회원용 홈 필터링 (ALL) V2
    public Slice<ItemHomeResponse> filterByAllV2(ItemFilterDto itemFilterDto, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterV2(itemFilterDto, pageable, keyword);

        List<ItemHomeResponse> tupleToList = listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0, Item.class);
                    Image image = tuple.get(3, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT: 비회원용 홈 필터링 (ALL) V3
    public List<ItemHomeResponse> getItemListFilterByAllForAnonymousV3(ItemFilterDto itemFilterDto, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByFilterV3(itemFilterDto, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : 회원용 홈 필터링 (ALL)
    public Slice<ItemHomeResponse> filterByAll(Long userId, ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilter(userId, itemFilterDto, pageable);

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
    public Slice<ItemHomeResponse> filterByAllV2(Long userId, ItemFilterDto itemFilterDto, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterV2(userId, itemFilterDto, pageable, keyword);

        List<ItemHomeResponse> tupleToList =  listOfTuple
                .stream()
                .map(tuple -> {
                    Item item = tuple.get(0,Item.class);
                    UserItem userItem = tuple.get(1, UserItem.class);
                    Image image = tuple.get(4, Image.class);

                    ItemHomeResponse itemHomeResponse = new ItemHomeResponse(item, image.getUrl());
                    if(userItem != null) {
                        itemHomeResponse.likesOfMe();
                    }

                    return itemHomeResponse;
                })
                .collect(Collectors.toList());

        return toSlice(tupleToList, pageable);
    }

    // FEAT : 회원용 홈 필터링 (ALL) V3
    public List<ItemHomeResponse> getItemListFilterByAllForUserV3(Long userId, ItemFilterDto itemFilterDto, Long itemId) {

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterV3(userId, itemFilterDto, itemId);

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

    public MypageResponse findMyItem(Long userId, TradeStatus status) {

        List<ItemSummaryDto> myItems = itemRepositoryCustom.findAllByUserIdAndTradeStatus(userId, status)
                .stream()
                .map(item -> ItemSummaryDto.of(item, item.getImages().get(0).getUrl()))
                .collect(Collectors.toList());

        List<Item> itemsByUserId = itemRepository.findByUserId(userId);

        // 찜 Count
        Long countOfLikes = userItemRepository.countByUserId(userId);

        // 후기 Count
        Long countOfReceivedReviews = reviewRepositoryCustom.countByReveiverId(userId);

        // 가격제시 Count
        Long countOfReceievedPriceProposes = priceProposeRepositoryCustom.countSuggestedInItems(itemsByUserId);

        MypageResponse mypageResponse = new MypageResponse(myItems);

        mypageResponse.setCountOfLikes(countOfLikes);
        mypageResponse.setCountOfReceivedReviews(countOfReceivedReviews);
        mypageResponse.setCountOfReceievedPriceProposes(countOfReceievedPriceProposes);

        return mypageResponse;
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

    // FEAT: 비회원용 검색 기능
    public List<ItemHomeResponse> getSearchedItemListForAnonymous(String keyword, Long itemId) {

        List<String> keywords = List.of(keyword.split(" "));

        List<Item> items = itemRepositoryCustom.findByKeywordWithLimit(keywords, itemId);
        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT: 회원용 검색 기능
    public List<ItemHomeResponse> getSearchedItemListForUser(String keyword, Long userId, Long itemId) {

        List<String> keywords = List.of(keyword.split(" "));

        User user = userRepository.findById(userId).get();
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findByKeywordWithUserItemAndLimit(userId, keywords, itemId);

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

    public List<ItemSummaryDto> getItemsOfOtherUser(String bcryptId, TradeStatus tradeStatus) {

        User user = userRepository.findByBcryptId(bcryptId);
        return itemRepositoryCustom.findAllByUserIdAndTradeStatus(user.getId(), tradeStatus)
                .stream()
                .map(item -> new ItemSummaryDto(item))
                .collect(Collectors.toList());
    }

    public ItemSummaryDto showDetailSummary(Long itemId) {
        return ItemSummaryDto.of(itemRepository.findById(itemId)
                .orElseThrow(() -> {throw new NoResultException("Item not founded.");
                }));
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

    public HomeResponse getSearchedItemList(Long userId, String keyword, Long itemId) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getSearchedItemListForAnonymous(keyword, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = getSearchedItemListForUser(keyword, userId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, new LoginUser(user), itemList);
        }
    }

    public HomeResponse getItemListV3(Long userId, Long itemId) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;

        // HINT : 비회원에게 보여줄 홈
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListForAnonymousV3(itemId);

            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@LLLLLLLLL");
            System.out.println(itemList.size());

            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = getItemListForUserV3(userId, itemId);
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println(itemList.size());

            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, new LoginUser(user), itemList);
        }
    }

    public HomeResponse getItemListFilterByIdolGroupV3(Long userId,Long idolGroupId, Long itemId) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;

        // HINT : 비회원에게 보여줄 홈 + 아이돌 필터링
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListFilterByIdolGroupForAnonoymousV3(idolGroupId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 아이돌 필터링
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = getItemListFilterByIdolGroupForUserV3(userId, idolGroupId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, new LoginUser(user), itemList);
        }
    }

    public HomeResponse getItemListFilterByAllV3(Long userId, ItemFilterDto itemFilterDto, Long itemId) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;

        // HINT : 비회원에게 보여줄 홈 + 모든 필터링
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListFilterByAllForAnonymousV3(itemFilterDto, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : 회원에게 보여줄 홈 + 모든 필터링
        else {
            User user = userRepository.findById(userId).get();
            List<ItemHomeResponse> itemList = getItemListFilterByAllForUserV3(userId, itemFilterDto, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, new LoginUser(user), itemList);
        }
    }

    public List<CategoryResponse> getItemCategory() {
        return itemCategoryRepository.findAll()
                .stream()
                .map(itemCategory -> new CategoryResponse(itemCategory))
                .collect(Collectors.toList());
    }
}

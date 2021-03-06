package com.ducks.goodsduck.commons.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.ducks.goodsduck.commons.exception.common.InvalidStateException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.exception.image.ImageProcessException;
import com.ducks.goodsduck.commons.exception.image.InvalidMetadataException;
import com.ducks.goodsduck.commons.model.dto.ItemFilterDto;
import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.home.HomeResponse;
import com.ducks.goodsduck.commons.model.dto.item.*;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationMessage;
import com.ducks.goodsduck.commons.model.dto.user.MypageResponse;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import com.ducks.goodsduck.commons.model.enums.*;
import com.ducks.goodsduck.commons.model.redis.ClickItemDataRedis;
import com.ducks.goodsduck.commons.repository.chat.ChatRepository;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.idol.IdolGroupVoteRedisTemplate;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepository;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepositoryCustom;
import com.ducks.goodsduck.commons.repository.category.ItemCategoryRepository;
import com.ducks.goodsduck.commons.repository.idol.IdolMemberRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepositoryCustom;
import com.ducks.goodsduck.commons.repository.image.ItemImageRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepositoryCustom;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepositoryCustom;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepository;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepositoryCustom;
import com.ducks.goodsduck.commons.util.FcmUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
    private final DeviceRepositoryCustom deviceRepositoryCustom;
    private final IdolGroupVoteRedisTemplate idolGroupVoteRedisTemplate;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    private final ImageUploadService imageUploadService;

    public Long upload(ItemUploadRequest itemUploadRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        Item item = new Item(itemUploadRequest);

        /** Item-User ???????????? ?????? **/
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                new Object[]{"User"}, null)));
        item.setUser(user);

        /** Item-IdolMember ???????????? ?????? **/
        IdolMember idolMember = idolMemberRepository.findById(itemUploadRequest.getIdolMember())
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                new Object[]{"IdolMember"}, null)));
        item.setIdolMember(idolMember);

        /** Item-Category ???????????? ?????? **/
        ItemCategory itemCategory = itemCategoryRepository.findByName(itemUploadRequest.getCategory());
        item.setItemCategory(itemCategory);

        /** ????????? ????????? ?????? & Item-Image ???????????? ?????? **/
        List<Image> images = new ArrayList<>();
        try {
            images = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM, user.getNickName());
        } catch (ImageProcessingException e) {
            log.debug("Exception occured during processing ItemImage: {}", e.getMessage(), e.getStackTrace());
            throw new ImageProcessException();
        } catch (MetadataException e) {
            log.debug("Exception occured during reading Metadata of Item: {}", e.getMessage(), e.getStackTrace());
            throw new InvalidMetadataException();
        }
        for (Image image : images) {
            ItemImage itemImage = new ItemImage(image);
            item.addImage(itemImage);
            imageRepository.save(itemImage);
        }

        idolGroupVoteRedisTemplate.addCountUploadByUserId(userId);
        itemRepository.save(item);

        if (user.gainExpByType(ActivityType.ITEM) >= 100){
            if (user.getLevel() == null) user.setLevel(1);
            boolean success = user.levelUp();
            if(success) {
                List<String> registrationTokensByUserId = deviceRepositoryCustom.getRegistrationTokensByUserId(user.getId());
                FcmUtil.sendMessage(NotificationMessage.ofLevelUp(), registrationTokensByUserId);
            }
        }

        if (itemUploadRequest.getTradeType().equals(TradeType.SELL)) user.getVoteByActivity(ActivityType.ITEM_SELL);

        return item.getId();
    }

    public ItemDetailResponse showDetail(Long itemId) {

        Item item = itemRepository.findById(itemId).get();
        if(item == null || item.getDeletedAt() != null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"Item"}, null));
        }

        item.increaseView();

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);

        List<PricePropose> priceProposes = priceProposeRepositoryCustom.findAllByItemId(itemId);
        itemDetailResponse.addProposedList(priceProposes);

        return itemDetailResponse;
    }

    public ItemDetailResponse showDetailWithLike(Long userId, Long itemId) throws JsonProcessingException {

        User loginUser = userRepository.findById(userId).orElse(null);
        if(loginUser == null) {
            return showDetail(itemId);
        }

        Tuple itemTupleWithUserItem = itemRepositoryCustom.findByIdWithUserItem(userId, itemId);
        Item item = itemTupleWithUserItem.get(0, Item.class);
        if(item == null || item.getDeletedAt() != null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"Item"}, null));
        }

        item.increaseView();

        ItemDetailResponse itemDetailResponse = new ItemDetailResponse(item);
        itemDetailResponse.setLoginUser(new UserSimpleDto(loginUser));

        if (itemTupleWithUserItem.get(1, long.class) > 0L) {
            itemDetailResponse.likesOfMe();
        }

        // ?????? ?????? ?????? ?????? ??????
        List<PricePropose> priceProposes = priceProposeRepositoryCustom.findAllByItemId(itemId);
        itemDetailResponse.addProposedList(priceProposes);

        // ????????? ????????? ??????
        if (item.getUser().getId().equals(userId)) {
            // ??? ????????? ??????
            itemDetailResponse.myItem();
        } else {
            // ?????? ?????? ?????? ??????
            PricePropose pricePropose = priceProposeRepositoryCustom.findByUserIdAndItemId(userId, itemId);
            if(pricePropose != null) {
                itemDetailResponse.addMyPricePropose(pricePropose);
            }

            // ????????? ??????
            Chat chat = userChatRepositoryCustom.findByUserIdAndItemId(userId, itemId);
            if(chat != null) {
                itemDetailResponse.setChatId(chat.getId());
            }
        }

        // ?????? ?????? ????????? ??????
        IdolMember idolMember = item.getIdolMember();
        IdolGroup idolGroup = idolMember.getIdolGroup();
        ClickItemDataRedis clickItemDataRedis = new ClickItemDataRedis(userId, item, idolGroup.getId(), idolMember.getId());

        return itemDetailResponse;
    }

    public Long edit(Long itemId, ItemUpdateRequest itemUpdateRequest) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));;
        item.setName(itemUpdateRequest.getName());
        item.setDescription(itemUpdateRequest.getDescription());
        item.setPrice(itemUpdateRequest.getPrice());
        item.setTradeType(itemUpdateRequest.getTradeType());
        item.setGradeStatus(itemUpdateRequest.getGradeStatus());
        IdolMember idolMember = idolMemberRepository.findById(itemUpdateRequest.getIdolMember())
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"IdolMember"}, null)));
        item.setIdolMember(idolMember);
        ItemCategory itemCategory = itemCategoryRepository.findByName(itemUpdateRequest.getCategory());
        item.setItemCategory(itemCategory);

        return item.getId();
    }

    public Long editV2(Long itemId, ItemUpdateRequestV2 itemUpdateRequest, List<MultipartFile> multipartFiles, Long userId) throws IOException {

        // TODO : ???????????? ??? ?????? ??????
        
        /**
         * ?????? ????????? ?????? ??????
         * ??????
         * ??????
         * ??????
         * ?????? (S, A, B, C)
         * ?????? (BUY, SELL)
         * ????????? ??????
         * ????????????
         */
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));
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
        IdolMember idolMember = idolMemberRepository.findById(itemUpdateRequest.getIdolMember())
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"IdolMember"}, null)));
        item.setIdolMember(idolMember);
        ItemCategory itemCategory = itemCategoryRepository.findByName(itemUpdateRequest.getCategory());
        item.setItemCategory(itemCategory);

        /**
         * ?????? ????????? ?????? (Url)
         * case1. ?????? ????????? ????????? ?????? -> ?????? ????????? ????????????
         * case2. ?????? ????????? ?????? ????????? ?????? (null, empty)
         * case3. ?????? ????????? ?????? ????????? ??????
         *
         * ????????? ????????? ?????? (??????)
         * case4. ????????? ????????? ???????????? ?????? ?????? -> ?????? ????????? ????????????
         * case5. ????????? ????????? ????????? ??????
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
            List<Image> images = null;
            try {
                images = imageUploadService.uploadImages(multipartFiles, ImageType.ITEM, user.getNickName());
            } catch (IOException e) {
                log.debug("IOException occured during uploading ItemImage: {}", e.getMessage(), e.getStackTrace());
                throw new IOException();
            } catch (ImageProcessingException e) {
                log.debug("Exception occured during processing ItemImage: {}", e.getMessage(), e.getStackTrace());
                throw new ImageProcessException();
            } catch (MetadataException e) {
                log.debug("Exception occured during reading Metadata of Item: {}", e.getMessage(), e.getStackTrace());
                throw new InvalidMetadataException();
            }
            for (Image image : images) {
                ItemImage itemImage = new ItemImage(image);
                item.addImage(itemImage);
                imageRepository.save(itemImage);
            }
        }

        return item.getId();
    }

    public Boolean delete(Long itemId) {

        Item deleteItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));

        // image ?????? ??????
        List<ItemImage> deleteImages = deleteItem.getImages();
        itemImageRepository.deleteInBatch(deleteImages);

        // pricePropose ?????? ??????
        List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(itemId);
        priceProposeRepository.deleteInBatch(deletePriceProposes);

        // userChat ?????? ??????
        List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(itemId);
        userChatRepository.deleteInBatch(deleteUserChats);

        // chat ??????
        List<Chat> deleteChats = new ArrayList<>();
        for (UserChat deleteUserChat : deleteUserChats) {
            deleteChats.add(deleteUserChat.getChat());
        }
        chatRepository.deleteInBatch(deleteChats);

        // review ?????? ??????
        List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(itemId);
        for (Review deleteItemOfReview : deleteItemOfReviews) {
            deleteItemOfReview.setItem(null);
        }

        // userItem ?????? ??????
        List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(itemId);
        userItemRepository.deleteInBatch(deleteUserItems);

        // item ??????
        itemRepository.delete(deleteItem);

        return true;
    }

    public Boolean deleteV2(Long itemId) {
        Item deleteItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));

        // image ?????? ??????
        List<ItemImage> deleteItemImages = deleteItem.getImages();
        for (Image deleteImage : deleteItemImages) {
            deleteImage.setDeletedAt(LocalDateTime.now());
        }

        // pricePropose ?????? ??????
        List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(itemId);
        for (PricePropose deletePricePropose : deletePriceProposes) {
            deletePricePropose.setDeletedAt(LocalDateTime.now());
        }

        // userChat, chat ?????? ??????
        List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(itemId);
        for (UserChat deleteUserChat : deleteUserChats) {
            deleteUserChat.setDeletedAt(LocalDateTime.now());
            deleteUserChat.getChat().setDeletedAt(LocalDateTime.now());
        }

        // userItem ?????? ??????
        List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(itemId);
        for (UserItem deleteUserItem : deleteUserItems) {
            deleteUserItem.setDeletedAt(LocalDateTime.now());
        }

        // Item ??????
        deleteItem.setDeletedAt(LocalDateTime.now());

        return true;
    }

    public Boolean isItemOwner(Long userId, Long itemId) {

        Long findUserId = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)))
                .getUser().getId();

        if(userId.equals(findUserId)) {
            return true;
        } else {
            return false;
        }
    }

    public Optional<Item> getDetails(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public Item upload(ItemUploadRequest itemUploadRequest) {
        return itemRepository.save(new Item(itemUploadRequest));
    }

    // FEAT : ???????????? ??? (V1)
    public Slice<ItemHomeResponse> getItemList(Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAll(pageable);
        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT : ???????????? ??? (V2)
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

    // FEAT : ???????????? ??? (V3)
    public List<ItemHomeResponse> getItemListForAnonymousV3(Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllV3(itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : ????????? ???
    public Slice<ItemHomeResponse> getItemList(Long userId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
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

    // FEAT : ????????? ??? (V2)
    public Slice<ItemHomeResponse> getItemListUserV2(Long userId, Integer pageNumber, String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
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

    // FEAT : ????????? ??? (V3)
    public List<ItemHomeResponse> getItemListForUserV3(Long userId, Long itemId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByUserIdolGroupsWithUserItemV4(userId, userIdolGroups, itemId, user.getBlockedUserIds(), user.getBlockedItemIds());

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

    // FEAT : ???????????? ??? ????????? (????????? ??????)
    public Slice<ItemHomeResponse> filterByIdolGroup(Long idolGroupId, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByIdolGroup(idolGroupId, pageable);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT : ???????????? ??? ????????? (????????? ??????) V2
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

    // FEAT : ???????????? ??? ????????? (????????? ??????) V3
    public List<ItemHomeResponse> getItemListFilterByIdolGroupForAnonoymousV3(Long idolGroupId, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByIdolGroupV3(idolGroupId, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : ????????? ??? ????????? (???????????????)
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

    // FEAT : ????????? ??? ????????? (???????????????) V2
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

    // FEAT : ????????? ??? ????????? (???????????????) V3
    public List<ItemHomeResponse> getItemListFilterByIdolGroupForUserV3(Long userId, Long idolGroupId, Long itemId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByIdolGroupWithUserItemV4(userId, idolGroupId, itemId, user.getBlockedUserIds(), user.getBlockedItemIds());

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

    // FEAT: ???????????? ??? ????????? (ALL)
    public Slice<ItemHomeResponse> filterByAll(ItemFilterDto itemFilterDto, Integer pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, PropertyUtil.PAGEABLE_SIZE);

        List<Item> items = itemRepositoryCustom.findAllByFilter(itemFilterDto, pageable);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return toSlice(itemToList, pageable);
    }

    // FEAT: ???????????? ??? ????????? (ALL) V2
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

    // FEAT: ???????????? ??? ????????? (ALL) V3
    public List<ItemHomeResponse> getItemListFilterByAllForAnonymousV3(ItemFilterDto itemFilterDto, Long itemId) {

        List<Item> items = itemRepositoryCustom.findAllByFilterV3(itemFilterDto, itemId);

        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT : ????????? ??? ????????? (ALL)
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

    // FEAT : ????????? ??? ????????? (ALL) V2
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

    // FEAT : ????????? ??? ????????? (ALL) V3
    public List<ItemHomeResponse> getItemListFilterByAllForUserV3(Long userId, ItemFilterDto itemFilterDto, Long itemId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        List<Tuple> listOfTuple = itemRepositoryCustom.findAllByFilterV4(userId, itemFilterDto, itemId, user.getBlockedUserIds(), user.getBlockedItemIds());

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
                .map(item -> ItemSummaryDto.of(item))
                .collect(Collectors.toList());

        List<Item> itemsByUserId = itemRepository.findByUserId(userId);

        // ??? Count
        Long countOfLikes = userItemRepository.countByUserId(userId);

        // ?????? Count
        Long countOfReceivedReviews = reviewRepositoryCustom.countByReveiverId(userId);

        // ???????????? Count
        Long countOfReceievedPriceProposes = priceProposeRepositoryCustom.countSuggestedInItems(itemsByUserId);

        MypageResponse mypageResponse = new MypageResponse(myItems);

        mypageResponse.setCountOfLikes(countOfLikes);
        mypageResponse.setCountOfReceivedReviews(countOfReceivedReviews);
        mypageResponse.setCountOfReceievedPriceProposes(countOfReceievedPriceProposes);

        return mypageResponse;
    }

    public boolean updateTradeStatus(Long userId, Long itemId, TradeStatus status) {
        Optional<Item> findItemOpt = itemRepository.findById(itemId);

        Item findItem = findItemOpt
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                new Object[]{"Item"}, null)));

        if (findItem.getTradeStatus().equals(COMPLETE)) throw new InvalidStateException("Already completed trade item.");

        TradeType tradeType = findItem.getTradeType();

        switch (status) {
            case BUYING:
                if (tradeType.equals(TradeType.SELL)) throw new InvalidStateException("This item's tradeType is SELLING");
                break;
            case SELLING:
                if (tradeType.equals(TradeType.BUY)) throw new InvalidStateException("This item's tradeType is BUYING");
                break;
        }

        return itemRepositoryCustom.updateTradeStatus(itemId, status) > 0 ? true : false;
    }

    // FEAT: ???????????? ?????? ??????
    public List<ItemHomeResponse> getSearchedItemListForAnonymous(String keyword, Long itemId, Long price, Order order, Boolean complete) {

        List<Item> items = itemRepositoryCustom.findByKeywordWithLimit(keyword, itemId, price, order, complete);
        List<ItemHomeResponse> itemToList =  items
                .stream()
                .map(item -> new ItemHomeResponse(item))
                .collect(Collectors.toList());

        return itemToList;
    }

    // FEAT: ????????? ?????? ??????
    public List<ItemHomeResponse> getSearchedItemListForUser(String keyword, Long userId, Long itemId, Long price, Order order, Boolean complete) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));
        user.updateLastLoginAt();
        List<UserIdolGroup> userIdolGroups = user.getUserIdolGroups();

        List<Tuple> listOfTuple = itemRepositoryCustom.findByKeywordWithUserItemAndLimit(userId, keyword, itemId, price, order, complete);

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

    public HomeResponse getSearchedItemList(Long userId, String keyword, Long itemId, Long price, Order order, Boolean complete) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        Boolean hasNext= false;

        // HINT : ?????? - ?????????
        if(price == null) {

            // HINT : ??????????????? ????????? ???
            if(userId.equals(-1L)) {
                List<ItemHomeResponse> itemList = getSearchedItemListForAnonymous(keyword, itemId, price, order, complete);
                if(itemList.size() == pageableSize + 1) {
                    hasNext = true;
                    itemList.remove(pageableSize);
                }

                return new HomeResponse(hasNext, null, itemList);
            }
            // HINT : ???????????? ????????? ???
            else {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                                new Object[]{"User"}, null)));
                List<ItemHomeResponse> itemList = getSearchedItemListForUser(keyword, userId, itemId, price, order, complete);
                if(itemList.size() == pageableSize + 1) {
                    hasNext = true;
                    itemList.remove(pageableSize);
                }

                return new HomeResponse(hasNext, new LoginUser(user), itemList);
            }
        }
        // HINT : ???????????? - ?????????
        else {

            // HINT : ??????????????? ????????? ???
            if(userId.equals(-1L)) {
                List<ItemHomeResponse> itemList = getSearchedItemListForAnonymous(keyword, itemId, price, order, complete);
                if(itemList.size() == pageableSize + 1) {
                    hasNext = true;
                    itemList.remove(pageableSize);
                }

                return new HomeResponse(hasNext, null, itemList);
            }
            // HINT : ???????????? ????????? ???
            else {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                                new Object[]{"User"}, null)));
                List<ItemHomeResponse> itemList = getSearchedItemListForUser(keyword, userId, itemId, price, order, complete);
                if(itemList.size() == pageableSize + 1) {
                    hasNext = true;
                    itemList.remove(pageableSize);
                }

                return new HomeResponse(hasNext, new LoginUser(user), itemList);
            }
        }
    }

    public HomeResponse getItemListV3(Long userId, Long itemId) {

        int pageableSize = PropertyUtil.PAGEABLE_SIZE;
        boolean hasNext= false;

        // HINT : ??????????????? ????????? ???
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListForAnonymousV3(itemId);

            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : ???????????? ????????? ???
        else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"User"}, null)));
            List<ItemHomeResponse> itemList = getItemListForUserV3(userId, itemId);

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

        // HINT : ??????????????? ????????? ??? + ????????? ?????????
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListFilterByIdolGroupForAnonoymousV3(idolGroupId, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : ???????????? ????????? ??? + ????????? ?????????
        else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"User"}, null)));
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

        // HINT : ??????????????? ????????? ??? + ?????? ?????????
        if(userId.equals(-1L)) {
            List<ItemHomeResponse> itemList = getItemListFilterByAllForAnonymousV3(itemFilterDto, itemId);
            if(itemList.size() == pageableSize + 1) {
                hasNext = true;
                itemList.remove(pageableSize);
            }

            return new HomeResponse(hasNext, null, itemList);
        }
        // HINT : ???????????? ????????? ??? + ?????? ?????????
        else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                            new Object[]{"User"}, null)));
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

    public Item findById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));
    }

    public ItemSummaryDto showDetailSummary(Long itemId) {

        Item item = itemRepository.findById(itemId).
                orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));

        if(item.getDeletedAt() != null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"Item"}, null));
        }

        return ItemSummaryDto.of(item);
    }

    public List<Long> getMyItemNumbersNotCompleted(Long userId) {
        return itemRepositoryCustom.findAllByUserIdAndNotCompleted(userId);
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

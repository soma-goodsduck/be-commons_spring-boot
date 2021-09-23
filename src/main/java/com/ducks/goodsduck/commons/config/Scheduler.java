package com.ducks.goodsduck.commons.config;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.repository.category.ItemCategoryRepository;
import com.ducks.goodsduck.commons.repository.chat.ChatRepository;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.idol.IdolMemberRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepository;
import com.ducks.goodsduck.commons.repository.image.ImageRepositoryCustom;
import com.ducks.goodsduck.commons.repository.image.ItemImageRepository;
import com.ducks.goodsduck.commons.repository.image.ProfileImageRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepositoryCustom;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepository;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepositoryCustom;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepositoryCustom;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepository;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepositoryCustom;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class Scheduler {

//    private final UserRepository userRepository;
//    private final ItemRepository itemRepository;
//    private final ChatRepository chatRepository;
//    private final UserChatRepository userChatRepository;
//    private final UserChatRepositoryCustom userChatRepositoryCustom;
//    private final UserItemRepository userItemRepository;
//    private final UserItemRepositoryCustom userItemRepositoryCustom;
//    private final PriceProposeRepository priceProposeRepository;
//    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;
//    private final ReviewRepositoryCustom reviewRepositoryCustom;
//    private final ImageRepository imageRepository;
//    private final ItemImageRepository itemImageRepository;
//    private final ProfileImageRepository profileImageRepository;
//
//    private final ImageUploadService imageUploadService;
//
//    @Scheduled(cron = "0 0 0 * * *")
//    public void itemDelete() {
//
//        List<Item> deleteItems = itemRepository.findAllWithDeleted();
//
//        for (Item deleteItem : deleteItems) {
//
//            // HINT : 30일 지난 후 삭제
//            if(deleteItem.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {
//
//                log.info("cron delete item : " + deleteItem.getId());
//
//                // user's item 목록 삭제
//                User user = deleteItem.getUser();
//                List<Item> itemsOfUser = user.getItems();
//                itemsOfUser.remove(deleteItem);
//
//                // image 연관 삭제
//                List<ItemImage> deleteImages = deleteItem.getImages();
//                for (ItemImage deleteImage : deleteImages) {
//                    imageUploadService.deleteImage(deleteImage, ImageType.ITEM);
//                }
//                itemImageRepository.deleteInBatch(deleteImages);
//
//                // pricePropose 연관 삭제
//                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(deleteItem.getId());
//                priceProposeRepository.deleteInBatch(deletePriceProposes);
//
//                // userChat 연관 삭제
//                List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(deleteItem.getId());
//                userChatRepository.deleteInBatch(deleteUserChats);
//
//                // chat 삭제
//                List<Chat> deleteChats = new ArrayList<>();
//                for (UserChat deleteUserChat : deleteUserChats) {
//                    deleteChats.add(deleteUserChat.getChat());
//                }
//                chatRepository.deleteInBatch(deleteChats);
//
//                // review 연관 삭제
//                List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(deleteItem.getId());
//                for (Review deleteItemOfReview : deleteItemOfReviews) {
//                    deleteItemOfReview.setItem(null);
//                }
//
//                // userItem 연관 삭제
//                List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(deleteItem.getId());
//                userItemRepository.deleteInBatch(deleteUserItems);
//
//                // item 삭제
//                itemRepository.delete(deleteItem);
//            }
//        }
//    }
//
//    @Scheduled(cron = "*/5 * * * * *")
//    public void userDelete() {
//
//        List<User> deleteUsers = userRepository.findAllWithDeleted();
////
////        User user = userRepository.findById(2L).get();
////
////        Image image = imageRepository.findByUrl(user.getImageUrl());
////        imageRepository.delete(image);
//
//        for (User deleteUser : deleteUsers) {
//
//            // HINT : 30일 지난 후 삭제
//            if(deleteUser.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {
//
//                log.info("cron delete user : " + deleteUser.getId());
//
//                // image 연관 삭제 (profile)
//                if(!deleteUser.getImageUrl().equals(PropertyUtil.BASIC_IMAGE_URL)) {
//                    Image deleteImage = imageRepository.findByUrl(deleteUser.getImageUrl());
//                    imageUploadService.deleteImage(deleteImage, ImageType.PROFILE);
//                    imageRepository.delete(deleteImage);
//                }
//
//                // pricePropose 연관 삭제
//                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(deleteUser.getId());
//                priceProposeRepository.deleteInBatch(deletePriceProposes);
//
//                // userChat 연관 삭제
//                List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(deleteUser.getId());
//                userChatRepository.deleteInBatch(deleteUserChats);
//
//                // chat 삭제
//                List<Chat> deleteChats = new ArrayList<>();
//                for (UserChat deleteUserChat : deleteUserChats) {
//                    deleteChats.add(deleteUserChat.getChat());
//                }
//                chatRepository.deleteInBatch(deleteChats);
//
//                // review 연관 삭제
//                List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(deleteUser.getId());
//                for (Review deleteItemOfReview : deleteItemOfReviews) {
//                    deleteItemOfReview.setItem(null);
//                }
//
//                // userItem 연관 삭제
//                List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(deleteUser.getId());
//                userItemRepository.deleteInBatch(deleteUserItems);
//
//                // item 삭제
//                itemRepository.delete(deleteUser);
//            }
//        }
//    }
}

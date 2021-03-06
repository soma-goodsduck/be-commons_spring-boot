package com.ducks.goodsduck.commons.config;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.Image.PostImage;
import com.ducks.goodsduck.commons.model.enums.ImageType;
import com.ducks.goodsduck.commons.repository.chat.ChatRepository;
import com.ducks.goodsduck.commons.repository.image.*;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.post.UserPostRepository;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepository;
import com.ducks.goodsduck.commons.repository.pricepropose.PriceProposeRepositoryCustom;
import com.ducks.goodsduck.commons.repository.review.ReviewRepositoryCustom;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import com.ducks.goodsduck.commons.repository.user.UserRepositoryCustom;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepository;
import com.ducks.goodsduck.commons.repository.userchat.UserChatRepositoryCustom;
import com.ducks.goodsduck.commons.repository.useridolgroup.UserIdolGroupRepository;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepository;
import com.ducks.goodsduck.commons.repository.useritem.UserItemRepositoryCustom;
import com.ducks.goodsduck.commons.service.ImageUploadService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final ItemRepository itemRepository;
    private final ChatRepository chatRepository;
    private final UserChatRepository userChatRepository;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final UserItemRepository userItemRepository;
    private final UserItemRepositoryCustom userItemRepositoryCustom;
    private final PriceProposeRepository priceProposeRepository;
    private final PriceProposeRepositoryCustom priceProposeRepositoryCustom;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final ImageRepository imageRepository;
    private final ItemImageRepository itemImageRepository;
    private final UserIdolGroupRepository userIdolGroupRepository;
    private final UserPostRepository userPostRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ImageUploadService imageUploadService;

    @Scheduled(cron = "0 0 0 * * *")
    public void itemDelete() {

        List<Item> deleteItems = itemRepository.findAllWithDeleted();

        for (Item deleteItem : deleteItems) {

            // HINT : 30??? ?????? ??? ??????
            if(deleteItem.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete item : " + deleteItem.getId());

                // image ?????? ??????
                List<ItemImage> deleteImages = deleteItem.getImages();
                for (ItemImage deleteImage : deleteImages) {
                    imageUploadService.deleteImage(deleteImage, ImageType.ITEM);
                }
                itemImageRepository.deleteInBatch(deleteImages);

                // pricePropose ?????? ??????
                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(deleteItem.getId());
                priceProposeRepository.deleteInBatch(deletePriceProposes);

                // userChat ?????? ??????
                List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(deleteItem.getId());
                userChatRepository.deleteInBatch(deleteUserChats);

                // chat ??????
                List<Chat> deleteChats = new ArrayList<>();
                for (UserChat deleteUserChat : deleteUserChats) {
                    deleteChats.add(deleteUserChat.getChat());
                }
                chatRepository.deleteInBatch(deleteChats);

                // review ?????? ??????
                List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(deleteItem.getId());
                for (Review deleteItemOfReview : deleteItemOfReviews) {
                    deleteItemOfReview.setItem(null);
                }

                // userItem ?????? ??????
                List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(deleteItem.getId());
                userItemRepository.deleteInBatch(deleteUserItems);

                // item ??????
                itemRepository.delete(deleteItem);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void userDelete() {

        List<User> deleteUsers = userRepository.findAllWithDeleted();

        for (User deleteUser : deleteUsers) {

            // HINT : 30??? ?????? ??? ??????
            if(deleteUser.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete user : " + deleteUser.getId());

                // image ?????? ?????? (profile)
                if(!deleteUser.getImageUrl().equals(PropertyUtil.BASIC_IMAGE_URL)) {
                    Image deleteImage = imageRepository.findByUrl(deleteUser.getImageUrl());
                    imageUploadService.deleteImage(deleteImage, ImageType.PROFILE);
                    imageRepository.delete(deleteImage);
                }

                // pricePropose ?????? ??????
                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByUserIdWithAllStatus(deleteUser.getId());
                priceProposeRepository.deleteInBatch(deletePriceProposes);

                // userChat ?????? ??????
                List<UserChat> deleteUserChats = userChatRepository.findByUserId(deleteUser.getId());
                userChatRepository.deleteInBatch(deleteUserChats);

                // chat ??????
                List<Chat> deleteChats = new ArrayList<>();
                for (UserChat deleteUserChat : deleteUserChats) {
                    deleteChats.add(deleteUserChat.getChat());
                }
                chatRepository.deleteInBatch(deleteChats);

                // userItem ?????? ??????
                List<UserItem> deleteUserItems = userItemRepository.findAllByUserId(deleteUser.getId());
                userItemRepository.deleteInBatch(deleteUserItems);

                // userIdolGroup ?????? ??????
                List<UserIdolGroup> deleteUserIdolGroups = deleteUser.getUserIdolGroups();
                userIdolGroupRepository.deleteInBatch(deleteUserIdolGroups);
                
                // userPost ?????? ??????
                List<UserPost> deleteUserPosts = userPostRepository.findAllByUserId(deleteUser.getId());
                userPostRepository.deleteInBatch(deleteUserPosts);

                // email, password, phone, nickname, image, level ????????? ????????? ????????? ??????
                deleteUser.setEmail(null);
                deleteUser.setPhoneNumber(null);
                deleteUser.setPassword(null);
                deleteUser.setNickName("????????? ?????????");
                deleteUser.setImageUrl(PropertyUtil.BASIC_IMAGE_URL);
                deleteUser.setLevel(1);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void postDelete() {

        List<Post> deletePosts = postRepository.findAllWithDeleted();

        for (Post deletePost : deletePosts) {

            // HINT : 30??? ?????? ??? ??????
            if(deletePost.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete post : " + deletePost.getId());

                // image ?????? ??????
                List<PostImage> deleteImages = deletePost.getImages();
                for (PostImage deleteImage : deleteImages) {
                    imageUploadService.deleteImage(deleteImage, ImageType.POST);
                }
                postImageRepository.deleteInBatch(deleteImages);

                // userPost ?????? ??????
                List<UserPost> deleteUserPosts = userPostRepository.findAllByPostId(deletePost.getId());
                userPostRepository.deleteInBatch(deleteUserPosts);

                // post ??????
                postRepository.delete(deletePost);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void initializeVoteInfo() {
        userRepositoryCustom.initializeVotedIdolGroupIdAll();
//        userRepositoryCustom.addDailyVoteAll();
        userRepositoryCustom.initializeGrantOfAttendAll();
    }
}

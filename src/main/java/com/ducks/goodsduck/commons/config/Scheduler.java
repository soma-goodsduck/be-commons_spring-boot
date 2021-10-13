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

            // HINT : 30일 지난 후 삭제
            if(deleteItem.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete item : " + deleteItem.getId());

                // image 연관 삭제
                List<ItemImage> deleteImages = deleteItem.getImages();
                for (ItemImage deleteImage : deleteImages) {
                    imageUploadService.deleteImage(deleteImage, ImageType.ITEM);
                }
                itemImageRepository.deleteInBatch(deleteImages);

                // pricePropose 연관 삭제
                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByItemIdWithAllStatus(deleteItem.getId());
                priceProposeRepository.deleteInBatch(deletePriceProposes);

                // userChat 연관 삭제
                List<UserChat> deleteUserChats = userChatRepositoryCustom.findByItemId(deleteItem.getId());
                userChatRepository.deleteInBatch(deleteUserChats);

                // chat 삭제
                List<Chat> deleteChats = new ArrayList<>();
                for (UserChat deleteUserChat : deleteUserChats) {
                    deleteChats.add(deleteUserChat.getChat());
                }
                chatRepository.deleteInBatch(deleteChats);

                // review 연관 삭제
                List<Review> deleteItemOfReviews = reviewRepositoryCustom.findByItemId(deleteItem.getId());
                for (Review deleteItemOfReview : deleteItemOfReviews) {
                    deleteItemOfReview.setItem(null);
                }

                // userItem 연관 삭제
                List<UserItem> deleteUserItems = userItemRepositoryCustom.findByItemId(deleteItem.getId());
                userItemRepository.deleteInBatch(deleteUserItems);

                // item 삭제
                itemRepository.delete(deleteItem);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void userDelete() {

        List<User> deleteUsers = userRepository.findAllWithDeleted();

        for (User deleteUser : deleteUsers) {

            // HINT : 30일 지난 후 삭제
            if(deleteUser.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete user : " + deleteUser.getId());

                // image 연관 삭제 (profile)
                if(!deleteUser.getImageUrl().equals(PropertyUtil.BASIC_IMAGE_URL)) {
                    Image deleteImage = imageRepository.findByUrl(deleteUser.getImageUrl());
                    imageUploadService.deleteImage(deleteImage, ImageType.PROFILE);
                    imageRepository.delete(deleteImage);
                }

                // pricePropose 연관 삭제
                List<PricePropose> deletePriceProposes = priceProposeRepositoryCustom.findAllByUserIdWithAllStatus(deleteUser.getId());
                priceProposeRepository.deleteInBatch(deletePriceProposes);

                // userChat 연관 삭제
                List<UserChat> deleteUserChats = userChatRepository.findByUserId(deleteUser.getId());
                userChatRepository.deleteInBatch(deleteUserChats);

                // chat 삭제
                List<Chat> deleteChats = new ArrayList<>();
                for (UserChat deleteUserChat : deleteUserChats) {
                    deleteChats.add(deleteUserChat.getChat());
                }
                chatRepository.deleteInBatch(deleteChats);

                // userItem 연관 삭제
                List<UserItem> deleteUserItems = userItemRepository.findAllByUserId(deleteUser.getId());
                userItemRepository.deleteInBatch(deleteUserItems);

                // userIdolGroup 연관 삭제
                List<UserIdolGroup> deleteUserIdolGroups = deleteUser.getUserIdolGroups();
                userIdolGroupRepository.deleteInBatch(deleteUserIdolGroups);
                
                // userPost 연관 삭제
                List<UserPost> deleteUserPosts = userPostRepository.findAllByUserId(deleteUser.getId());
                userPostRepository.deleteInBatch(deleteUserPosts);

                // email, password, phone, nickname, image, level 탈퇴한 사용자 정보로 변경
                deleteUser.setEmail(null);
                deleteUser.setPhoneNumber(null);
                deleteUser.setPassword(null);
                deleteUser.setNickName("탈퇴한 사용자");
                deleteUser.setImageUrl(PropertyUtil.BASIC_IMAGE_URL);
                deleteUser.setLevel(1);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void postDelete() {

        List<Post> deletePosts = postRepository.findAllWithDeleted();

        for (Post deletePost : deletePosts) {

            // HINT : 30일 지난 후 삭제
            if(deletePost.getDeletedAt().plusDays(30).isBefore(LocalDateTime.now())) {

                log.info("cron delete post : " + deletePost.getId());

                // image 연관 삭제
                List<PostImage> deleteImages = deletePost.getImages();
                for (PostImage deleteImage : deleteImages) {
                    imageUploadService.deleteImage(deleteImage, ImageType.POST);
                }
                postImageRepository.deleteInBatch(deleteImages);

                // userPost 연관 삭제
                List<UserPost> deleteUserPosts = userPostRepository.findAllByPostId(deletePost.getId());
                userPostRepository.deleteInBatch(deleteUserPosts);

                // post 삭제
                postRepository.delete(deletePost);
            }
        }
    }

    // HINT: 자정 마다 투표했던 아이돌 그룹 ID를 0으로 초기화 (클라이언트에서 ID 유무로 투표 참여 여부 체크)
    @Scheduled(cron = "0 0 0 * * *")
    public void initializeVoteInfo() {
        userRepositoryCustom.initializeVotedIdolGroupIdAll();
        userRepositoryCustom.addDailyVoteAll();
    }
}

package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.review.ReviewRequest;
import com.ducks.goodsduck.commons.model.dto.review.ReviewResponse;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Review;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserChat;
import com.ducks.goodsduck.commons.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final ItemRepository itemRepository;

    public ReviewService(ReviewRepository reviewRepository, ReviewRepositoryCustomImpl reviewRepositoryCustom, UserChatRepositoryCustomImpl userChatRepositoryCustom, ItemRepository itemRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewRepositoryCustom = reviewRepositoryCustom;
        this.userChatRepositoryCustom = userChatRepositoryCustom;
        this.itemRepository = itemRepository;
    }

    public Optional<Review> saveReview(Long senderId, ReviewRequest reviewRequest) throws IllegalAccessException {

        String chatRoomId = reviewRequest.getChatRoomId();
        UserChat receiverChat = userChatRepositoryCustom.findBySenderIdAndChatRoomId(senderId, chatRoomId);
        Long receiverId = receiverChat.getUser().getId();
        Long itemId = reviewRequest.getItemId();

        // HINT: 자신에게 남기는 셀프 리뷰 불가능
        if (senderId.equals(receiverId)) {
            throw new IllegalAccessException("It's not be able to write review by self.");
        }

        // HINT: 리뷰 중복 방지
        if (reviewRepositoryCustom.existsByItemIdAndSenderIdAndReceiverId(itemId, senderId, receiverId)) {
            throw new IllegalAccessException("Review of this trade already exists.");
        }

        User sender = userChatRepositoryCustom.findSenderByChatIdAndUserId(chatRoomId, senderId);
        Item tradeCompletedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    throw new NoResultException("Item not founded.");
                });

        // HINT: 채팅에 참여한 사용자에 한해서 리뷰 작성 가능
        if (sender == null) {
            throw new IllegalAccessException("Reviewer must be in chat room.");
        }

        return Optional.ofNullable(reviewRepository.save(new Review(sender, tradeCompletedItem, receiverId, reviewRequest.getContent(), reviewRequest.getScore())));
    }

    public List<ReviewResponse> getReviewsOfItemOwner(Long itemId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    throw new NoResultException("Owner of item not founded.");
                });

        return reviewRepositoryCustom.findByReveiverId(item.getUser().getId())
                .stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsOfLoginUser(Long userId) {

        return reviewRepositoryCustom.findByReveiverId(userId)
                .stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }
}

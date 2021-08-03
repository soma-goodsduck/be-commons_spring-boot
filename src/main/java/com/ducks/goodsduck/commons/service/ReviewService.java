package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.ReviewRequest;
import com.ducks.goodsduck.commons.model.dto.ReviewResponse;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Review;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.*;
import com.querydsl.core.Tuple;
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

        // HINT: 리뷰 중복 방지
        if (reviewRepositoryCustom.existsByItemIdAndUserId(reviewRequest.getItemId(), senderId)) {
            throw new IllegalAccessException("Review of this trade already exists.");
        }

        Tuple senderAndItem = userChatRepositoryCustom.findSenderAndItemByChatIdAndUserId(chatRoomId, senderId);

        // HINT: 채팅에 참여한 사용자에 한해서 리뷰 작성 가능
        if (senderAndItem == null) {
            throw new IllegalAccessException("Reviewer must be in chat room.");
        }

        User sender = senderAndItem.get(0, User.class);
        Item tradedItem = senderAndItem.get(1, Item.class);

        if (senderId.equals(tradedItem.getUser().getId())) {
            throw new IllegalAccessException("It's not be able to write review by self.");
        }

        return Optional.ofNullable(reviewRepository.save(new Review(sender, tradedItem, reviewRequest.getContent())));
    }

    public List<ReviewResponse> getReviewsOfItemOwner(Long itemId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    throw new NoResultException("Owner of item not founded.");
                });

        List<Item> itemsByUserId = itemRepository.findByUserId(item.getUser().getId());

        return reviewRepositoryCustom.findInItems(itemsByUserId)
                .stream()
                .map(tuple -> {
                    Review review = tuple.get(0, Review.class);
                    return new ReviewResponse(review);
                })
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsOfLoginUser(Long userId) {
        List<Item> itemsByUserId = itemRepository.findByUserId(userId);

        return reviewRepositoryCustom.findInItems(itemsByUserId)
                .stream()
                .map(tuple -> {
                    Review review = tuple.get(0, Review.class);
                    return new ReviewResponse(review);
                })
                .collect(Collectors.toList());
    }
}

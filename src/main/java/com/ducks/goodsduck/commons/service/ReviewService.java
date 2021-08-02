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

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRepositoryCustom itemRepositoryCustom;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, UserChatRepositoryCustomImpl userChatRepositoryCustom, ItemRepository itemRepository, ItemRepositoryCustomImpl itemRepositoryCustom) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.userChatRepositoryCustom = userChatRepositoryCustom;
        this.itemRepository = itemRepository;
        this.itemRepositoryCustom = itemRepositoryCustom;
    }


    public Optional<Review> saveReview(Long senderId, ReviewRequest reviewRequest) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> {
                    throw new NoResultException("User not founded.");
                });

        Tuple receieverAndItem = userChatRepositoryCustom.findReceieverAndItemByChatId(reviewRequest.getChatRoomId(), senderId);

        Item tradedItem = receieverAndItem.get(0, Item.class);
        User receiver = receieverAndItem.get(1, User.class);

        return Optional.ofNullable(reviewRepository.save(new Review(receiver, tradedItem, reviewRequest.getContent())));
    }

    public List<ReviewResponse> getReviewsOfItemOwner(Long itemId) {

        itemRepository.findById(itemId);

        Tuple itemAndUser = itemRepositoryCustom.findItemAndUserByItemId(itemId);

        Item item = itemAndUser.get(0, Item.class);
        User user = itemAndUser.get(1, User.class);

        return reviewRepository.findByUserId(user.getId())
                .stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsOfLoginUser(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(review -> new ReviewResponse(review))
                .collect(Collectors.toList());
    }
}

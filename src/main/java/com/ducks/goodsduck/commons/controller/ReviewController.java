package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.review.ReviewRequest;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.review.ReviewResponse;
import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.entity.Review;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.ItemRepository;
import com.ducks.goodsduck.commons.repository.ItemRepositoryCustom;
import com.ducks.goodsduck.commons.repository.ItemRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.service.ReviewService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.OK;

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = "유저에 대한 리뷰 APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final NotificationService notificationService;

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRepositoryCustom itemRepositoryCustom;

    public ReviewController(ReviewService reviewService, NotificationService notificationService, UserRepository userRepository, ItemRepository itemRepository, ItemRepositoryCustomImpl itemRepositoryCustom) {
        this.reviewService = reviewService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRepositoryCustom = itemRepositoryCustom;
    }

    @GetMapping("/v1/users/reviews")
    @ApiOperation("마이 페이지에서 리뷰 목록 조회")
    public ApiResult<List<ReviewResponse>> getMyReviews(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(reviewService.getReviewsOfLoginUser(userId));
    }

    @PostMapping("/v1/users/reviews")
    @ApiOperation("채팅방 ID를 통해 특정 유저에 대한 리뷰 남기기")
    public ApiResult<Boolean> sendReview(HttpServletRequest request,
                                         @RequestBody ReviewRequest reviewRequest) throws IllegalAccessException {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        Review savedReview = reviewService.saveReview(userId, reviewRequest)
                .orElseThrow(() -> {
                    throw new IllegalStateException("Error occurred during save review.");
                });

        User receiver = userRepository.findById(savedReview.getReceiverId()).orElseThrow(() -> {
            throw new NoResultException("User not founded.");
        });
        notificationService.sendMessage(new Notification(savedReview, receiver));
        return OK(true);
    }

    @GetMapping("/v1/items/{itemId}/users/reviews")
    @ApiOperation("특정 아이템 게시물 작성자에 대한 리뷰 목록 조회")
    public ApiResult<List<ReviewResponse>> getReviews(@PathVariable("itemId") Long itemId) {

        return OK(reviewService.getReviewsOfItemOwner(itemId));
    }
}

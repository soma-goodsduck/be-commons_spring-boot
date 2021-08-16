package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
import com.ducks.goodsduck.commons.model.enums.ReviewType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id @GeneratedValue
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private String senderNickName;
    private String senderImageUrl;
    private NotificationType type;

    private Long itemId;
    private String itemName;

    // HINT: REVIEW, PRICE_PROPOSE 인 경우만 필요
    private String itemImageUrl;

    // HINT: REVIEW 인 경우만 필요
    private Long reviewId;

    // HINT: PRICE_PROPOSE 인 경우만 필요
    private Long priceProposeId;
    private Integer price;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification(User user, PriceProposeResponse priceProposeResponse) {
        this.user = user;
        this.senderNickName = priceProposeResponse.getProposer().getNickName();
        this.senderImageUrl = priceProposeResponse.getProposer().getImageUrl();
        this.itemId = priceProposeResponse.getItem().getItemId();
        this.itemName = priceProposeResponse.getItem().getName();
        this.itemImageUrl = priceProposeResponse.getItem().getImageUrl();
        this.type = NotificationType.PRICE_PROPOSE;
        this.priceProposeId = priceProposeResponse.getPriceProposeId();
        this.price = priceProposeResponse.getProposedPrice();
        this.createdAt = priceProposeResponse.getCreatedAt();
    }

    public Notification(User user, UserItem userItem) {
        this.user = user;
        User proposer = userItem.getUser();
        this.senderNickName = proposer.getNickName();
        this.senderImageUrl = proposer.getImageUrl();
        this.itemId = userItem.getItem().getId();
        this.itemName = userItem.getItem().getName();
        this.type = NotificationType.USER_ITEM;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Review review, User receiver, ReviewType reviewType) {
        this.user = receiver;
        this.senderNickName = review.getUser().getNickName();
        this.senderImageUrl = review.getUser().getImageUrl();
        this.reviewId = review.getId();
        this.itemId = review.getItem().getId();
        this.itemName = review.getItem().getName();
        this.itemImageUrl = review.getItem().getImages().get(0).getUrl();
        if (reviewType.equals(ReviewType.REVIEW)) this.type = NotificationType.REVIEW;
        else if (reviewType.equals(ReviewType.REVIEW_FIRST)) this.type = NotificationType.REVIEW_FIRST;
        this.createdAt = review.getCreatedAt();
    }

    public Notification(User user, String senderNickname, String senderImageUrl, String itemName, NotificationType type) {
        this.user = user;
        this.senderNickName = senderNickname;
        this.senderImageUrl = senderImageUrl;
        this.itemName = itemName;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(User user, String senderNickname, String senderImageUrl, Long itemId, String itemName, String itemImageUrl, NotificationType type, Integer price) {
        this.user = user;
        this.senderNickName = senderNickname;
        this.senderImageUrl = senderImageUrl;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImageUrl = itemImageUrl;
        this.type = type;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
}

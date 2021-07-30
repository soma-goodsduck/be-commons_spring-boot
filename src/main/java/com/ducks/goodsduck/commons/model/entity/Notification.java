package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
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
    private String senderImageUri;
    private String itemName;
    private NotificationType type;

    // HINT: Notification.PRICE_PROPOSE 인 경우만 필요
    private Integer price;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification(User user, PriceProposeResponse priceProposeResponse) {
        this.user = user;
        this.senderNickName = priceProposeResponse.getProposer().getNickName();
        this.senderImageUri = priceProposeResponse.getProposer().getImageUrl();
        this.itemName = priceProposeResponse.getItem().getName();
        this.type = NotificationType.PRICE_PROPOSE;
        this.price = priceProposeResponse.getProposedPrice();
        this.createdAt = LocalDateTime.now();
    }

    public Notification(User user, String senderNickname, String senderImageUri, String itemName, NotificationType type) {
        this.user = user;
        this.senderNickName = senderNickname;
        this.senderImageUri = senderImageUri;
        this.itemName = itemName;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(User user, String senderNickname, String senderImageUri, String itemName,   NotificationType type, Integer price) {
        this.user = user;
        this.senderNickName = senderNickname;
        this.senderImageUri = senderImageUri;
        this.itemName = itemName;
        this.type = type;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
}

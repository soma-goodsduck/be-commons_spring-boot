package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricePropose {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRICE_PROPOSE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    @Enumerated(EnumType.STRING)
    private PriceProposeStatus status;

    private int price;
    private LocalDateTime createdAt;

    public PricePropose(User user, Item item, int price) {
        this.user = user;
        this.item = item;
        this.status = PriceProposeStatus.SUGGESTED;
        this.price = price;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
    }
}

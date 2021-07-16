package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.enums.StatusGrade;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long id;

    /** USER 테이블과의 다대일 관계 정의 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDOL_MEMBER_ID")
    private IdolMember idolMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ITEM_ID")
    private CategoryItem categoryItem;

    private String name;
    private int price;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    private StatusGrade statusGrade;

    private String imageUrl;
    private String description;
    private LocalDateTime itemCreatedAt;
    private LocalDateTime updatedAt;
    private int views;
    private int likesItemCount;

    public Item(ItemUploadRequest itemUploadRequest) {
        this.name = itemUploadRequest.getName();
        this.price = itemUploadRequest.getPrice();
        this.tradeType = itemUploadRequest.getTradeType();
        this.tradeStatus = itemUploadRequest.getTradeStatus();
        this.statusGrade = itemUploadRequest.getStatusGrade();
        this.imageUrl = itemUploadRequest.getImageUrl();
        this.description = itemUploadRequest.getDescription();
        this.itemCreatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.views = 0;
        this.likesItemCount = 0;
    }
}

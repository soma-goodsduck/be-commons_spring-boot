package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.item.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.ItemImage;
import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    private String name;
    private Long price;
    private String description;
    private Integer views;
    private Integer likesItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    private GradeStatus gradeStatus;

    @OneToMany(mappedBy = "item")
    private List<ItemImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_member_id")
    private IdolMember idolMember;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    public Item(ItemUploadRequest itemUploadRequest) {
        this.name = itemUploadRequest.getName();
        this.price = itemUploadRequest.getPrice();
        this.tradeType = itemUploadRequest.getTradeType();
        this.gradeStatus = itemUploadRequest.getGradeStatus();
        this.description = itemUploadRequest.getDescription();
        this.views = 0;
        this.likesItemCount = 0;
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"));
        if(tradeType.equals(TradeType.BUY)) {
            this.tradeStatus = TradeStatus.BUYING;
        } else {
            this.tradeStatus = TradeStatus.SELLING;
        }
    }

    public Image getThumbNail() {
        return this.images.get(0);
    }

    public void addImage(ItemImage image) {
        image.setItem(this);
        this.images.add(image);
    }

    public Item liked() {
        this.likesItemCount++;
        return this;
    }

    public Item unLiked() {
        this.likesItemCount--;
        return this;
    }

    public void increaseView() {
        this.views++;
    }
}

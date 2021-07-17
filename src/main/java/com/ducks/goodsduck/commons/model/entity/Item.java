package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.ItemUploadRequest;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;
    private String name;
    private Long price;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likesItemCount;
    private Integer views;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    private GradeStatus gradeStatus;

    @OneToMany(mappedBy = "item")
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idol_member_id")
    private IdolMember idolMember;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_item_id")
    private CategoryItem categoryItem;

    public Item(ItemUploadRequest itemUploadRequest) {
        this.name = itemUploadRequest.getName();
        this.price = itemUploadRequest.getPrice();
        this.tradeType = itemUploadRequest.getTradeType();
        this.gradeStatus = itemUploadRequest.getGradeStatus();
        this.description = itemUploadRequest.getDescription();
        this.views = 0;
        this.likesItemCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if(tradeType.equals(TradeType.BUY)) {
            this.tradeStatus = TradeStatus.BUYING;
        } else {
            this.tradeStatus = TradeStatus.FOR_SALE;
        }
    }

    public Image getThumbNail() {
        // TODO : images List가 empty일 경우
        return this.images.get(0);
    }

    public void addImage(Image image) {
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
}

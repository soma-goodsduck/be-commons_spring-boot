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
    private Long likesItemCount;

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
    @JoinColumn(name = "category_id")
    private Category category;

    public Item(ItemUploadRequest itemUploadRequest) {
        this.name = itemUploadRequest.getName();
        this.price = itemUploadRequest.getPrice();
        this.tradeType = itemUploadRequest.getTradeType();
        this.gradeStatus = itemUploadRequest.getGradeStatus();
        this.description = itemUploadRequest.getDescription();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if(tradeType.equals("구매")) {
            this.tradeStatus = TradeStatus.구매중;
        } else {
            this.tradeStatus = TradeStatus.판매중;
        }
    }

    public void addImage(Image image) {
        image.setItem(this);
        this.images.add(image);
    }
}

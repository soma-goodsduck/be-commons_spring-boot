package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemDetailResponse {

    private ItemDetailResponseUser user;
    private String name;
    private String description;
    private List<ItemDetailResponseImage> images = new ArrayList<>();
    private Long price;
    private TradeType tradeType;
    private GradeStatus gradeStatus;
    private ItemDetailResponseIdol idolMember;
    private LocalDateTime itemCreatedAt;
//    private ItemDetailResponseCategory category;
    private String categoryName;

    public ItemDetailResponse(Item item) {
        this.user = new ItemDetailResponseUser(item.getUser());
        this.name = item.getName();
        this.description = item.getDescription();
        this.images = item.getImages().stream()
                                        .map(image -> new ItemDetailResponseImage(image))
                                        .collect(Collectors.toList());
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.gradeStatus = item.getGradeStatus();
        this.idolMember = new ItemDetailResponseIdol(item.getIdolMember());
        this.itemCreatedAt = item.getCreatedAt();
//        this.category = new ItemDetailResponseCategory(item.getCategory());
        this.categoryName = item.getCategory().getName();
    }
}

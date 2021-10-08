package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.ImageDto;
import com.ducks.goodsduck.commons.model.dto.user.UserSimpleDto;
import com.ducks.goodsduck.commons.model.entity.IdolMember;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.category.ItemCategory;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeStatus;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemDto {

    private Long itemId;
    private String name;
    private Long price;
    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private GradeStatus gradeStatus;
    private String description;
    private Integer views;
    private Integer likesItemCount;
    private LocalDateTime itemCreatedAt;
    private LocalDateTime updatedAt;
    private Boolean isLike = false;
    private List<ImageDto> images = new ArrayList<>();
    private IdolMember idolMember;
    private UserSimpleDto userSimpleDto;
    private ItemCategory itemCategory;

    public ItemDto likesOfMe() {
        this.isLike = true;
        return this;
    }

    public ItemDto(Item item) {
        this.itemId = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.tradeType = item.getTradeType();
        this.tradeStatus = item.getTradeStatus();
        this.gradeStatus = item.getGradeStatus();
        this.description = item.getDescription();
        this.views = item.getViews();
        this.itemCreatedAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        this.likesItemCount = item.getLikesItemCount();
        this.images = item.getImages()
                        .stream()
                        .map(itemImage -> new ImageDto(itemImage))
                        .collect(Collectors.toList());
        this.idolMember = item.getIdolMember();
    }
}

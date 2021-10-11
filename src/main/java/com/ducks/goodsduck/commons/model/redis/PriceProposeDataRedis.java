package com.ducks.goodsduck.commons.model.redis;

import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.PricePropose;
import com.ducks.goodsduck.commons.model.enums.GradeStatus;
import com.ducks.goodsduck.commons.model.enums.LearningDataType;
import com.ducks.goodsduck.commons.model.enums.TradeType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.ducks.goodsduck.commons.model.enums.LearningDataType.PRICE_PROPOSE;

@Data
@NoArgsConstructor
@RedisHash(value = "pricePropose")
public class PriceProposeDataRedis implements Serializable {

    @Id @GeneratedValue
    private String id;
    private LearningDataType type = PRICE_PROPOSE;
    private Long userId;
    private Integer priceProposePrice;
    private Long itemId;
    private String itemName;
    private Long itemPrice;
    private String itemDescription;
    private Integer itemLikesItemCount;
    private TradeType tradeType;
    private GradeStatus gradeStatus;
    private Long idolGroupId;
    private Long idolMemberId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    // HINT: PRICE_PROPOSE
    public PriceProposeDataRedis(Long userId, PricePropose pricePropose, Item item, Long idolGroupId, Long idolMemberId) {
        this.priceProposePrice = pricePropose.getPrice();
        this.userId = userId;
        this.priceProposePrice = pricePropose.getPrice();
        this.itemId = item.getId();
        this.itemName = item.getName();
        this.itemPrice = item.getPrice();
        this.itemDescription = item.getDescription();
        this.itemLikesItemCount = item.getLikesItemCount();
        this.tradeType = item.getTradeType();
        this.gradeStatus = item.getGradeStatus();
        this.idolGroupId = idolGroupId;
        this.idolMemberId = idolMemberId;
        this.createdAt = LocalDateTime.now();
    }
}

package com.ducks.goodsduck.commons.model.dto.notification;

import com.ducks.goodsduck.commons.model.enums.NotificationType;
import com.ducks.goodsduck.commons.model.redis.NotificationRedis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRedisResponse implements Serializable {

    private String id;
    private NotificationType type;
    private Long reviewId;
    private Long priceProposeId;
    private Integer priceProposePrice;
    private String senderNickName;
    private Long itemId;
    private String itemName;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Boolean isRead;

    public void read() {
        this.isRead = true;
    }

    public NotificationRedisResponse(NotificationRedis notificationRedis) {
        this.id = notificationRedis.getId();
        this.type = notificationRedis.getType();
        this.reviewId = notificationRedis.getReviewId();
        this.priceProposeId = notificationRedis.getPriceProposeId();
        this.priceProposePrice = notificationRedis.getPriceProposePrice();
        this.senderNickName = notificationRedis.getSenderNickName();
        this.itemId = notificationRedis.getItemId();
        this.itemName = notificationRedis.getItemName();
        this.createdAt =  notificationRedis.getCreatedAt();
        this.expiredAt = notificationRedis.getExpiredAt();
        this.isRead = notificationRedis.getIsRead();
    }
}

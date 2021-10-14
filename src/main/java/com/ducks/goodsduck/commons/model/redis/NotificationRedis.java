package com.ducks.goodsduck.commons.model.redis;

import com.ducks.goodsduck.commons.model.enums.NotificationType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.ducks.goodsduck.commons.model.enums.NotificationType.PRICE_PROPOSE;
import static com.ducks.goodsduck.commons.model.enums.NotificationType.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRedis implements Serializable {

    @Id
    private String id;
    private NotificationType type;
    private Long reviewId;
    private Long priceProposeId;
    private Integer priceProposePrice;
    private String senderNickName;
    private String senderImageUrl;
    private Long itemId;
    private String itemName;
    private Long commentId;
    private Long postId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime expiredAt;
    private Boolean isRead;

    public void read() {
        this.isRead = true;
    }

    // HINT: REVIEW
    public NotificationRedis(NotificationType type, Long reviewId, Long itemId, String itemName, String senderNickName, String senderImageUrl) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.reviewId = reviewId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.senderNickName = senderNickName;
        this.senderImageUrl = senderImageUrl;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusWeeks(2L);
        this.isRead = false;
    }

    // HINT: PricePropose
    public NotificationRedis(Long priceProposeId, Integer priceProposePrice, Long itemId, String itemName, String senderNickName, String senderImageUrl) {
        this.id = UUID.randomUUID().toString();
        this.type = PRICE_PROPOSE;
        this.senderNickName = senderNickName;
        this.senderImageUrl = senderImageUrl;
        this.priceProposeId = priceProposeId;
        this.priceProposePrice = priceProposePrice;
        this.itemId = itemId;
        this.itemName = itemName;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusWeeks(2L);
        this.isRead = false;
    }

    // HINT: Comment, REPLY_COMMENT
    public NotificationRedis(Long commentId, Long postId, Long receiveCommentId, String senderNickName, String senderImageUrl) {
        this.id = UUID.randomUUID().toString();
        this.commentId = commentId;
        this.postId = postId;
        this.type = receiveCommentId == 0L ? COMMENT : REPLY_COMMENT;
        this.senderNickName = senderNickName;
        this.senderImageUrl = senderImageUrl;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusWeeks(2L);
        this.isRead = false;
    }
}

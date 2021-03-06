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
    private String senderImageUrl;
    private Long itemId;
    private String itemName;
    private Long commentId;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Boolean isRead;

    /** 메시지 부 */
    private NotificationMessage message;

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
        this.senderImageUrl = notificationRedis.getSenderImageUrl();
        this.itemId = notificationRedis.getItemId();
        this.itemName = notificationRedis.getItemName();
        this.commentId = notificationRedis.getCommentId();
        this.postId = notificationRedis.getPostId();
        this.createdAt =  notificationRedis.getCreatedAt();
        this.expiredAt = notificationRedis.getExpiredAt();
        this.isRead = notificationRedis.getIsRead();

        String title = "GOODSDUCK";
        String body = "";
        String messageUri = "";
        String iconUri = "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/sample_goodsduck.png";
        switch (type) {
            case PRICE_PROPOSE:
                body = body.concat(String.format("%s님이 \"%s\" 굿즈에 %s을 했어요. [%d원]", senderNickName,
                        itemName.length() < 12 ? itemName : itemName.substring(0, 12).concat("..."), type.getKorName(), priceProposePrice));
                messageUri = messageUri.concat(String.format("price/%d", itemId));
                break;

            case CHAT:
                body = body.concat(String.format("%s님이 \"%s\" 굿즈에 %s를 보냈어요.", senderNickName,
                        itemName.length() < 12 ? itemName : itemName.substring(0, 12).concat("..."), type.getKorName()));
                messageUri = messageUri.concat("chatting");
                break;

            case REVIEW:
                body = String.format("%s님이 %s를 남겼어요.", senderNickName, type.getKorName());
                messageUri = messageUri.concat("reviews");
                break;

            case REVIEW_FIRST:
                body = String.format("%s님이 %s를 남겼어요.\n감사 인사 겸 거래 리뷰를 남겨보세요!", senderNickName, type.getKorName());
                messageUri = messageUri.concat(String.format("review-back/%d", itemId));
                break;

            case COMMENT:
                body = String.format("%s님이 %s를 남겼어요.", senderNickName, type.getKorName());
                messageUri = messageUri.concat(String.format("post/%d", postId));
                break;

            case REPLY_COMMENT:
                body = String.format("%s님이 %s를 남겼어요.", senderNickName, type.getKorName());
                messageUri = messageUri.concat(String.format("post/%d", postId));
                break;

            default:
                body = body.concat(String.format("에 알림을 보냈어요."));
        }
        this.message = new NotificationMessage(
            title,
            body,
            messageUri,
            iconUri,
            type
        );
    }
}

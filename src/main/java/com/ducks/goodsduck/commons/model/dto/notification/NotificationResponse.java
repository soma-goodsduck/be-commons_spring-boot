package com.ducks.goodsduck.commons.model.dto.notification;

import com.ducks.goodsduck.commons.model.entity.Notification;
import com.ducks.goodsduck.commons.model.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotificationResponse {

    /** 메시지 구성 요소 */
    private String senderNickName;
    private String senderImageUrl;
    private Long itemId;
    private String itemName;
    private NotificationType type;
    private Integer price;
    private Long reviewId;
    private Long priceProposeId;
    private LocalDateTime createdAt;
    private Boolean isRead;

    /** 메시지 부 */
    private NotificationMessage message;

    public NotificationResponse(Notification notification) {
        this.senderNickName = notification.getSenderNickName();
        this.senderImageUrl = notification.getSenderImageUrl();
        this.itemId = notification.getItemId();
        this.itemName = notification.getItemName();
        this.type = notification.getType();
        this.price = notification.getPrice();
        this.reviewId = notification.getReviewId();
        this.priceProposeId = notification.getPriceProposeId();
        this.createdAt = notification.getCreatedAt();
        this.isRead = notification.getIsRead();

        String title = "GOODSDUCK";
        String itemTitle = notification.getItemName();
        String body = String.format("%s님이 \"%s\" 굿즈", senderNickName,
                itemTitle.length() < 12 ? notification.getItemName() : itemTitle.substring(0, 12).concat("..."));
        String messageUri = "";
        String iconUri = "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/sample_goodsduck.png";
        switch (type) {
            case PRICE_PROPOSE:
                body = body.concat(String.format("에 %s을 했어요. [%d원]", type.getKorName(), price));
                messageUri = messageUri.concat(String.format("price/%d", itemId));
                break;

            case REVIEW:
                body = String.format("%s님이 %s를 남겼어요.", senderNickName, type.getKorName());
                messageUri = messageUri.concat("reviews");
                break;

            case REVIEW_FIRST:
                body = String.format("%s님이 %s를 남겼어요.\n감사 인사 겸 %s를 남겨보세요!", senderNickName, type.getKorName(), type.getKorName());
                messageUri = messageUri.concat(String.format("review-back/%d", itemId));
                break;

            case CHAT:
                body = body.concat(String.format("에 %s를 보냈어요.", type.getKorName()));
                messageUri = messageUri.concat("chatting");
                break;

            default:
                body = body.concat(String.format("에 알림을 보냈어요."));
        }

        this.message = new NotificationMessage(
            title,
            body,
            messageUri,
            iconUri
        );
    }

    /**
     * @message 알림 유형별 구성
     *
     * 굿즈덕 @가격 제안(PricePropose) 알림
     * 제목: 굿즈덕 {notification.type} 알림
     * 내용: {user.nickname}님이 {item.name} 굿즈에 {notification.type}을 했어요. [{pricePropose.price}]
     *
     * 굿즈덕 @찜(UserItem) 알림
     * 제목: 굿즈덕 {notification.type} 알림
     * 내용: {user.nickname}님이 {item.name} 굿즈를 {notification.type}했어요.
     *
     * 굿즈덕 @채팅(Chat) 알림
     * 제목: 굿즈덕 {notification.type} 알림
     * 내용: {user.nickname}님이 {item.name} 굿즈에 {notification.type}을 보냈어요.
     *
     * 굿즈덕 @거래 리뷰(Review) 알림
     * 제목: 굿즈덕 {notification.type} 알림
     * 내용: {user.nickname}님이 {notification.type}을 남겼어요.
     */


}

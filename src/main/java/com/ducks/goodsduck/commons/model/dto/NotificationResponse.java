package com.ducks.goodsduck.commons.model.dto;

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
    private String senderImageUri;
    private String itemName;
    private NotificationType type;
    private Integer price;
    private LocalDateTime createdAt;
    private boolean isRead;

    /** 메시지 부 */
    private NotificationMessage message;

    public NotificationResponse(Notification notification) {
        this.senderNickName = notification.getSenderNickName();
        this.senderImageUri = notification.getSenderImageUri();
        this.itemName = notification.getItemName();
        this.type = notification.getType();
        this.price = notification.getPrice();
        this.createdAt = notification.getCreatedAt();
        this.isRead = notification.getReadAt() == null ? false : true;
        String title = String.format("굿즈덕 %s 알림", type);
        String itemTitle = notification.getItemName();
        String body = String.format("%s님이 \"%s\" 굿즈", senderNickName,
                itemTitle.length() < 12 ? notification.getItemName() : itemTitle.substring(12).concat("..."));
        String messageUri = "https://www.goods-duck.com"; // TODO
        String iconUri = "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/sample_goodsduck.png";
        switch (type) {
            case PRICE_PROPOSE:
                body = body.concat(String.format("에 %s을 했어요. [%d원]", type.getKorName(), price));
                break;

            case USER_ITEM:
                body = body.concat(String.format("를 %s했어요.", type.getKorName()));
                break;

            case CHAT:
                body = body.concat(String.format("에 %s를 보냈어요.", type.getKorName()));
                break;

            case REVIEW:
                body = String.format("%s님이 %s를 남겼어요.", senderNickName, type.getKorName());
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

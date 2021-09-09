package com.ducks.goodsduck.commons.model.enums;

public enum NotificationType {
    PRICE_PROPOSE("가격 제시"),
    USER_ITEM("찜"),
    CHAT("채팅"),
    REVIEW("거래 리뷰"),
    REVIEW_FIRST("선 거래 리뷰");

    private String korName;

    NotificationType(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}

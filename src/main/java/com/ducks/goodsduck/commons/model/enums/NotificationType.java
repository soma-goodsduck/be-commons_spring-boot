package com.ducks.goodsduck.commons.model.enums;

public enum NotificationType {
    PRICE_PROPOSE("가격제안"),
    USER_ITEM("찜"),
    CHAT("채팅");

    private String korName;

    NotificationType(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}

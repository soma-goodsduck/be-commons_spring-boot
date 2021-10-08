package com.ducks.goodsduck.commons.model.enums;

public enum NotificationType {
    PRICE_PROPOSE("가격 제시"),
    USER_ITEM("찜"),
    CHAT("채팅"),
    REVIEW("거래 리뷰"),
    REVIEW_FIRST("선 거래 리뷰"),
    LEVEL_UP("레벨업"),
    COMMENT("댓글"),
    REPLY_COMMENT("대댓글");

    private String korName;

    NotificationType(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}

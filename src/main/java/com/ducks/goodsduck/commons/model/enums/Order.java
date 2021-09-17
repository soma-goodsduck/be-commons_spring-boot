package com.ducks.goodsduck.commons.model.enums;

public enum Order {
    LATEST("최신순"),
    HIGH_PRICE("높은가격순"),
    LOW_PRICE("낮은가격순");

    private String korName;

    Order(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}

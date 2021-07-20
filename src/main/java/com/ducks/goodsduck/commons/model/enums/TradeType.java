package com.ducks.goodsduck.commons.model.enums;

public enum TradeType {
    BUY("구매"),
    SELL("판매");

    private String korName;

    TradeType(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}
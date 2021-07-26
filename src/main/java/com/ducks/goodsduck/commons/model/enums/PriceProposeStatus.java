package com.ducks.goodsduck.commons.model.enums;

public enum PriceProposeStatus {
    SUGGESTED("제안됨"),
    ACCEPTED("수락됨"),
    CANCELED("취소됨"),
    REFUSED("거절됨");

    private String korean;

    PriceProposeStatus(String korean) {
        this.korean = korean;
    }

    public String getKorean() {
        return korean;
    }
}

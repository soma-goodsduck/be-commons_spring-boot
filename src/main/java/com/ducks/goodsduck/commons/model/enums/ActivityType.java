package com.ducks.goodsduck.commons.model.enums;

public enum ActivityType {
    COMMENT(5),
    ITEM(10),
    REVIEW(20),
    POST(10);

    private int exp;

    ActivityType(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }
}

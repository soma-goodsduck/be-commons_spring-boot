package com.ducks.goodsduck.commons.model.enums;

public enum ActivityType {
    COMMENT(5, 0),
    ITEM(10, 0),
    REVIEW(20, 0),
    POST(10, 0),
    ITEM_SELL(0, 10),
    ITEM_BUY(0, 0);

    private int exp;
    private int vote;

    ActivityType(int exp) {
        this.exp = exp;
    }
    ActivityType(int exp, int vote) {
        this.exp = exp;
        this.vote = vote;
    }

    public int getExp() {
        return exp;
    }

    public int getVote() {
        return vote;
    }
}

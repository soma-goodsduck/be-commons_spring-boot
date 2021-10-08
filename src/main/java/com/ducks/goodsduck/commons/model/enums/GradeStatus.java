package com.ducks.goodsduck.commons.model.enums;

public enum GradeStatus {
    S("박스를 개봉하지 않은 새상품이며, 생산 당시의 포장상태가 그대로 보존된 완전한 상태입니다."),
    A("박스를 개봉한 중고 상품이며, 새 상품처럼 깨끗한 상태입니다."),
    B("사용 흔적이 있으나, 대체로 관리 상태가 양호한 중고 상품입니다."),
    C("사용 흔적이 많지만, 사용상에는 문제가 없는 상태입니다.");

    private String description;

    GradeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.ducks.goodsduck.commons.model.dto.home;

import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.dto.notification.NotificationBadgeResponse;
import lombok.Data;

import java.util.List;

@Data
public class HomeResponse<T> {

    Boolean hasNext;
    LoginUser loginUser;
    List<T> list;
    NotificationBadgeResponse noty;

    public HomeResponse(Boolean hasNext, LoginUser loginUser, List<T> list) {
        this.hasNext = hasNext;
        this.loginUser = loginUser;
        this.list = list;
        this.noty = new NotificationBadgeResponse();
    }

    public HomeResponse(Boolean hasNext, LoginUser loginUser, List<T> list, NotificationBadgeResponse noty) {
        this.hasNext = hasNext;
        this.loginUser = loginUser;
        this.list = list;
        this.noty = noty;
    }
}

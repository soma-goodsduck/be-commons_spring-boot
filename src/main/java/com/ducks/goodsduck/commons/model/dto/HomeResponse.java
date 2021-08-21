package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.dto.LoginUser;
import com.ducks.goodsduck.commons.model.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class HomeResponse<T> {

    Boolean hasNext;
    LoginUser loginUser;
    List<T> list;

    public HomeResponse(Boolean hasNext, LoginUser loginUser, List<T> list) {
        this.hasNext = hasNext;
        this.loginUser = loginUser;
        this.list = list;
    }
}

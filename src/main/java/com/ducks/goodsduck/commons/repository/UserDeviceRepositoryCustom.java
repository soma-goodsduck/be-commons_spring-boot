package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserDevice;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceRepositoryCustom {
    List<String> getRegistrationTokensByUserId(Long userId);
    Tuple getTupleByUserIdAndRegistrationToken(Long userId, String registrationToken);
}

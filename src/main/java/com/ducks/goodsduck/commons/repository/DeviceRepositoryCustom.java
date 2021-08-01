package com.ducks.goodsduck.commons.repository;

import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepositoryCustom {
    List<String> getRegistrationTokensByUserId(Long userId);
    Tuple getTupleByUserIdAndRegistrationToken(Long userId, String registrationToken);
}

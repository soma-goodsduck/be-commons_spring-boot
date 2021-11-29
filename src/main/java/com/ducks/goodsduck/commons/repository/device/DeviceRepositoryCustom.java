package com.ducks.goodsduck.commons.repository.device;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepositoryCustom {
    List<String> getRegistrationTokensByUserId(Long userId);
    List<String> getRegistrationTokensAll();
    List<String> getRegistrationTokensWithCursor(Long deviceId, Long offset);
    Long updateRegistrationTokenByUserId(Long userId, String registrationToken);
    Long disallowRegistrationTokenByUserId(Long userId);
}

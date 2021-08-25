package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.DeviceRepository;
import com.ducks.goodsduck.commons.repository.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.DeviceRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;

@Service
@Transactional
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceRepositoryCustom deviceRepositoryCustom;
    private final UserRepository userRepository;

    public DeviceService(DeviceRepository deviceRepository, DeviceRepositoryCustomImpl deviceRepositoryCustom, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceRepositoryCustom = deviceRepositoryCustom;
        this.userRepository = userRepository;
    }

    public Boolean register(Long userId, String registrationToken) {

        // HINT: registrationToken이 이미 존재하는 경우, 최근 발급 받은 토큰으로 업데이트.
        if (deviceRepository.existsByUserId(userId)) {
            return deviceRepositoryCustom.updateRegistrationTokenByUserId(userId, registrationToken) > 0 ? true : false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new NoResultException("User not founded.");
                });

        deviceRepository.save(new Device(user, registrationToken));
        return true;
    }

    public void discard(Long userId) {
        if (!deviceRepository.existsByUserId(userId)) {
            return;
        }

        deviceRepositoryCustom.disallowRegistrationTokenByUserId(userId);
    }
}

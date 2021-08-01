package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.DeviceRepository;
import com.ducks.goodsduck.commons.repository.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.DeviceRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.querydsl.core.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.UUID;

@Service
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

    public Device registerFCMToken(Long userId, String registrationToken) {

        // TODO: 기기 별 UUID 생성 및 관리 방법 구체화
        String uuid = UUID.randomUUID().toString();

        Tuple tupleOfUserAndDevice = deviceRepositoryCustom.getTupleByUserIdAndRegistrationToken(userId, registrationToken);
        if (tupleOfUserAndDevice == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                throw new NoResultException("User not founded.");
            });

            return deviceRepository.save(
                    new Device(user,
                        uuid, registrationToken));
        } else {
            log.debug("Already registered device.");
            return tupleOfUserAndDevice.get(1, Device.class);
        }
    }
}

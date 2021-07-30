package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.UserDevice;
import com.ducks.goodsduck.commons.repository.UserDeviceRepository;
import com.ducks.goodsduck.commons.repository.UserDeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.UserDeviceRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.UserRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.UUID;

@Service
@Slf4j
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserDeviceRepositoryCustom userDeviceRepositoryCustom;
    private final UserRepository userRepository;

    public UserDeviceService(UserDeviceRepository userDeviceRepository, UserDeviceRepositoryCustomImpl userDeviceRepositoryCustom, UserRepository userRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.userDeviceRepositoryCustom = userDeviceRepositoryCustom;
        this.userRepository = userRepository;
    }

    public UserDevice registerFCMToken(Long userId, String registrationToken) {

        // TODO: 기기 별 UUID 생성 및 관리 방법 구체화
        String uuid = UUID.randomUUID().toString();

        Tuple tupleOfUserAndUserDevice = userDeviceRepositoryCustom.getTupleByUserIdAndRegistrationToken(userId, registrationToken);
        if (tupleOfUserAndUserDevice == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                throw new NoResultException("User not founded.");
            });

            return userDeviceRepository.save(
                    new UserDevice(tupleOfUserAndUserDevice.get(0, User.class),
                        uuid, registrationToken));
        } else {
            log.debug("Already registered device.");
            return tupleOfUserAndUserDevice.get(1, UserDevice.class);
        }
    }
}

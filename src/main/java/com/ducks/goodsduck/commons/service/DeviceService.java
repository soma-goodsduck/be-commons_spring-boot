package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.device.DeviceRepository;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustom;
import com.ducks.goodsduck.commons.repository.device.DeviceRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
@Transactional
@Slf4j
public class DeviceService {

    private final EntityManager em;
    private final DeviceRepository deviceRepository;
    private final DeviceRepositoryCustom deviceRepositoryCustom;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public DeviceService(EntityManager em, DeviceRepository deviceRepository, DeviceRepositoryCustomImpl deviceRepositoryCustom, UserRepository userRepository, MessageSource messageSource) {
        this.em = em;
        this.deviceRepository = deviceRepository;
        this.deviceRepositoryCustom = deviceRepositoryCustom;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public Boolean register(Long userId, String registrationToken) {

        // HINT: registrationToken이 이미 존재하는 경우, 최근 발급 받은 토큰으로 업데이트.
        if (deviceRepository.existsByUserId(userId)) {
            return deviceRepositoryCustom.updateRegistrationTokenByUserId(userId, registrationToken) > 0 ? true : false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        deviceRepository.save(new Device(user, registrationToken));
        em.flush();
        em.clear();
        return true;
    }

    public void discard(Long userId) {
        if (!deviceRepository.existsByUserId(userId)) {
            return;
        }

        deviceRepositoryCustom.disallowRegistrationTokenByUserId(userId);
    }
}

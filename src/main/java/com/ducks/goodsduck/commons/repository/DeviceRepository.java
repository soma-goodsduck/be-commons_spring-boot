package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Boolean existsByUserId(Long userId);
    Device findByUser(User user);
}

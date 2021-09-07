package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Device;
import com.ducks.goodsduck.commons.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Boolean existsByUserId(Long userId);
    Optional<Device> findByUser(User user);
}

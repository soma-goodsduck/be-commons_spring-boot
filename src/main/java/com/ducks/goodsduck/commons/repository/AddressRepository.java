package com.ducks.goodsduck.commons.repository;

import com.ducks.goodsduck.commons.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Address findByUserId(Long userId);
}

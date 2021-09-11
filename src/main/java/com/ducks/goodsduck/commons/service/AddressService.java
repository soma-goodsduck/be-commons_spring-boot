package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.AddressDto;
import com.ducks.goodsduck.commons.model.entity.Address;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.repository.AddressRepository;
import com.ducks.goodsduck.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressDto getAddress(Long userId) {
        Address address = addressRepository.findByUserId(userId);
        return new AddressDto(address);
    }

    public Boolean registerAddress(Long userId, AddressDto addressDto) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoResultException("Not find user in AddressService.registerAddress"));

            Address address = new Address(addressDto);
            address.setUser(user);
            addressRepository.save(address);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean editAddress(Long userId, AddressDto addressDto) {
        try {
            Address address = addressRepository.findByUserId(userId);

            if(address == null) {
                throw new NoResultException("Not find user in AddressService.editAddress");
            }

            address.setName(addressDto.getName());
            address.setPhoneNumber(addressDto.getPhoneNumber());
            address.setDetailAddress(addressDto.getDetailAddress());

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

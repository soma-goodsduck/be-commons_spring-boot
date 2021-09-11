package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.Address;
import lombok.Data;

@Data
public class AddressDto {

    private String name;
    private String phoneNumber;
    private String detailAddress;

    public AddressDto(Address address) {
        this.name = address.getName();
        this.phoneNumber = address.getPhoneNumber();
        this.detailAddress = address.getDetailAddress();
    }
}

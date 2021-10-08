package com.ducks.goodsduck.commons.model.entity;

import com.ducks.goodsduck.commons.model.dto.AddressDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String phoneNumber;
    private String detailAddress;

    public Address(AddressDto addressDto) {
        this.name = addressDto.getName();
        this.phoneNumber = addressDto.getPhoneNumber();
        this.detailAddress = addressDto.getDetailAddress();
    }
}

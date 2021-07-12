package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id @GeneratedValue
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private String name;
    private String phoneNumber;
    private String fullAddress;

    public Address(User user, String name, String phoneNumber, String fullAddress) {
        this.user = user;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.fullAddress = fullAddress;
    }
}

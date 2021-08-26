package com.ducks.goodsduck.commons.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

    @Id @GeneratedValue
    @Column(name = "DEVICE_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private String registrationToken;
    private Boolean isAllowed;
    private LocalDateTime createdAt;

    public Device(User user, String registrationToken) {
        this.user = user;
        this.registrationToken = registrationToken;
        this.isAllowed = true;
        this.createdAt = LocalDateTime.now();
    }
}
